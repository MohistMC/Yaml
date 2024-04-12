/**
 * Copyright (c) 2008, SnakeYAML
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.mohistmc.snakeyaml.serializer;

import com.mohistmc.snakeyaml.DumperOptions;
import com.mohistmc.snakeyaml.DumperOptions.Version;
import com.mohistmc.snakeyaml.comments.CommentLine;
import com.mohistmc.snakeyaml.emitter.Emitable;
import com.mohistmc.snakeyaml.events.AliasEvent;
import com.mohistmc.snakeyaml.events.CommentEvent;
import com.mohistmc.snakeyaml.events.DocumentEndEvent;
import com.mohistmc.snakeyaml.events.DocumentStartEvent;
import com.mohistmc.snakeyaml.events.ImplicitTuple;
import com.mohistmc.snakeyaml.events.MappingEndEvent;
import com.mohistmc.snakeyaml.events.MappingStartEvent;
import com.mohistmc.snakeyaml.events.ScalarEvent;
import com.mohistmc.snakeyaml.events.SequenceEndEvent;
import com.mohistmc.snakeyaml.events.SequenceStartEvent;
import com.mohistmc.snakeyaml.events.StreamEndEvent;
import com.mohistmc.snakeyaml.events.StreamStartEvent;
import com.mohistmc.snakeyaml.nodes.AnchorNode;
import com.mohistmc.snakeyaml.nodes.MappingNode;
import com.mohistmc.snakeyaml.nodes.Node;
import com.mohistmc.snakeyaml.nodes.NodeId;
import com.mohistmc.snakeyaml.nodes.NodeTuple;
import com.mohistmc.snakeyaml.nodes.ScalarNode;
import com.mohistmc.snakeyaml.nodes.SequenceNode;
import com.mohistmc.snakeyaml.nodes.Tag;
import com.mohistmc.snakeyaml.resolver.Resolver;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Serializer {

    private final Emitable emitter;
    private final Resolver resolver;
    private final boolean explicitStart;
    private final boolean explicitEnd;
    private final Map<String, String> useTags;
    private final Set<Node> serializedNodes;
    private final Map<Node, String> anchors;
    private final AnchorGenerator anchorGenerator;
    private final Tag explicitRoot;
    private final boolean dereferenceAliases;
    private final Set<Node> recursive;
    private Version useVersion;
    private Boolean closed;

    public Serializer(Emitable emitter, Resolver resolver, DumperOptions opts, Tag rootTag) {
        if (emitter == null) {
            throw new NullPointerException("Emitter must  be provided");
        }
        if (resolver == null) {
            throw new NullPointerException("Resolver must  be provided");
        }
        if (opts == null) {
            throw new NullPointerException("DumperOptions must  be provided");
        }
        this.emitter = emitter;
        this.resolver = resolver;
        this.explicitStart = opts.isExplicitStart();
        this.explicitEnd = opts.isExplicitEnd();
        if (opts.getVersion() != null) {
            this.useVersion = opts.getVersion();
        }
        this.useTags = opts.getTags();
        this.serializedNodes = new HashSet<>();
        this.anchors = new HashMap<>();
        this.anchorGenerator = opts.getAnchorGenerator();
        this.dereferenceAliases = opts.isDereferenceAliases();
        this.recursive = Collections.newSetFromMap(new IdentityHashMap<>());
        this.closed = null;
        this.explicitRoot = rootTag;
    }

    public void open() throws IOException {
        if (closed == null) {
            this.emitter.emit(new StreamStartEvent(null, null));
            this.closed = Boolean.FALSE;
        } else if (Boolean.TRUE.equals(closed)) {
            throw new SerializerException("serializer is closed");
        } else {
            throw new SerializerException("serializer is already opened");
        }
    }

    public void close() throws IOException {
        if (closed == null) {
            throw new SerializerException("serializer is not opened");
        } else if (!Boolean.TRUE.equals(closed)) {
            this.emitter.emit(new StreamEndEvent(null, null));
            this.closed = Boolean.TRUE;
            // release unused resources
            this.serializedNodes.clear();
            this.anchors.clear();
            this.recursive.clear();
        }
    }

    public void serialize(Node node) throws IOException {
        if (closed == null) {
            throw new SerializerException("serializer is not opened");
        } else if (closed) {
            throw new SerializerException("serializer is closed");
        }
        this.emitter
                .emit(new DocumentStartEvent(null, null, this.explicitStart, this.useVersion, useTags));
        anchorNode(node);
        if (explicitRoot != null) {
            node.setTag(explicitRoot);
        }
        serializeNode(node, null);
        this.emitter.emit(new DocumentEndEvent(null, null, this.explicitEnd));
        this.serializedNodes.clear();
        this.anchors.clear();
        this.recursive.clear();
    }

    private void anchorNode(Node node) {
        if (node.getNodeId() == NodeId.anchor) {
            node = ((AnchorNode) node).getRealNode();
        }
        if (this.anchors.containsKey(node)) {
            String anchor = this.anchors.get(node);
            if (null == anchor) {
                anchor = this.anchorGenerator.nextAnchor(node);
                this.anchors.put(node, anchor);
            }
        } else {
            this.anchors.put(node,
                    node.getAnchor() != null ? this.anchorGenerator.nextAnchor(node) : null);
            switch (node.getNodeId()) {
                case sequence:
                    SequenceNode seqNode = (SequenceNode) node;
                    List<Node> list = seqNode.getValue();
                    for (Node item : list) {
                        anchorNode(item);
                    }
                    break;
                case mapping:
                    MappingNode mnode = (MappingNode) node;
                    List<NodeTuple> map = mnode.getValue();
                    for (NodeTuple object : map) {
                        Node key = object.keyNode();
                        Node value = object.valueNode();
                        anchorNode(key);
                        anchorNode(value);
                    }
                    break;
            }
        }
    }

    // parent Node is not used but might be used in the future
    private void serializeNode(Node node, Node parent) throws IOException {
        if (node.getNodeId() == NodeId.anchor) {
            node = ((AnchorNode) node).getRealNode();
        }
        if (dereferenceAliases && recursive.contains(node)) {
            throw new SerializerException("Cannot dereferenceAliases for recursive structures.");
        }
        recursive.add(node);
        String tAlias = !dereferenceAliases ? this.anchors.get(node) : null;
        if (!dereferenceAliases && this.serializedNodes.contains(node)) {
            this.emitter.emit(new AliasEvent(tAlias, null, null));
        } else {
            this.serializedNodes.add(node);
            switch (node.getNodeId()) {
                case scalar:
                    ScalarNode scalarNode = (ScalarNode) node;
                    serializeComments(node.getBlockComments());
                    Tag detectedTag = this.resolver.resolve(NodeId.scalar, scalarNode.getValue(), true);
                    Tag defaultTag = this.resolver.resolve(NodeId.scalar, scalarNode.getValue(), false);
                    ImplicitTuple tuple = new ImplicitTuple(node.getTag().equals(detectedTag),
                            node.getTag().equals(defaultTag));
                    ScalarEvent event = new ScalarEvent(tAlias, node.getTag().getValue(), tuple,
                            scalarNode.getValue(), null, null, scalarNode.getScalarStyle());
                    this.emitter.emit(event);
                    serializeComments(node.getInLineComments());
                    serializeComments(node.getEndComments());
                    break;
                case sequence:
                    SequenceNode seqNode = (SequenceNode) node;
                    serializeComments(node.getBlockComments());
                    boolean implicitS =
                            node.getTag().equals(this.resolver.resolve(NodeId.sequence, null, true));
                    this.emitter.emit(new SequenceStartEvent(tAlias, node.getTag().getValue(), implicitS,
                            null, null, seqNode.getFlowStyle()));
                    List<Node> list = seqNode.getValue();
                    for (Node item : list) {
                        serializeNode(item, node);
                    }
                    this.emitter.emit(new SequenceEndEvent(null, null));
                    serializeComments(node.getInLineComments());
                    serializeComments(node.getEndComments());
                    break;
                default:// instance of MappingNode
                    serializeComments(node.getBlockComments());
                    Tag implicitTag = this.resolver.resolve(NodeId.mapping, null, true);
                    boolean implicitM = node.getTag().equals(implicitTag);
                    MappingNode mnode = (MappingNode) node;
                    List<NodeTuple> map = mnode.getValue();
                    if (mnode.getTag() != Tag.COMMENT) {
                        this.emitter.emit(new MappingStartEvent(tAlias, mnode.getTag().getValue(), implicitM,
                                null, null, mnode.getFlowStyle()));
                        for (NodeTuple row : map) {
                            Node key = row.keyNode();
                            Node value = row.valueNode();
                            serializeNode(key, mnode);
                            serializeNode(value, mnode);
                        }
                        this.emitter.emit(new MappingEndEvent(null, null));
                        serializeComments(node.getInLineComments());
                        serializeComments(node.getEndComments());
                    }
            }
        }
        recursive.remove(node);
    }

    private void serializeComments(List<CommentLine> comments) throws IOException {
        if (comments == null) {
            return;
        }
        for (CommentLine line : comments) {
            CommentEvent commentEvent = new CommentEvent(line.getCommentType(), line.getValue(),
                    line.getStartMark(), line.getEndMark());
            this.emitter.emit(commentEvent);
        }
    }
}
