/**
 * Copyright (c) 2008, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.mohistmc.snakeyaml.composer;

import com.mohistmc.snakeyaml.DumperOptions.FlowStyle;
import com.mohistmc.snakeyaml.LoaderOptions;
import com.mohistmc.snakeyaml.comments.CommentEventsCollector;
import com.mohistmc.snakeyaml.comments.CommentLine;
import com.mohistmc.snakeyaml.comments.CommentType;
import com.mohistmc.snakeyaml.error.Mark;
import com.mohistmc.snakeyaml.error.YAMLException;
import com.mohistmc.snakeyaml.events.AliasEvent;
import com.mohistmc.snakeyaml.events.Event;
import com.mohistmc.snakeyaml.events.MappingStartEvent;
import com.mohistmc.snakeyaml.events.NodeEvent;
import com.mohistmc.snakeyaml.events.ScalarEvent;
import com.mohistmc.snakeyaml.events.SequenceStartEvent;
import com.mohistmc.snakeyaml.nodes.MappingNode;
import com.mohistmc.snakeyaml.nodes.Node;
import com.mohistmc.snakeyaml.nodes.NodeId;
import com.mohistmc.snakeyaml.nodes.NodeTuple;
import com.mohistmc.snakeyaml.nodes.ScalarNode;
import com.mohistmc.snakeyaml.nodes.SequenceNode;
import com.mohistmc.snakeyaml.nodes.Tag;
import com.mohistmc.snakeyaml.parser.Parser;
import com.mohistmc.snakeyaml.resolver.Resolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a node graph from parser events.
 * <p>
 * Corresponds to the 'Compose' step as described in chapter 3.1 of the
 * <a href="http://yaml.org/spec/1.1/">YAML Specification</a>.
 * </p>
 */
public class Composer {

  /**
   * its parser
   */
  protected final Parser parser;
  private final Resolver resolver;
  private final Map<String, Node> anchors;
  private final Set<Node> recursiveNodes;
  private int nonScalarAliasesCount = 0;
  private final LoaderOptions loadingConfig;
  private final CommentEventsCollector blockCommentsCollector;
  private final CommentEventsCollector inlineCommentsCollector;
  // keep the nesting of collections inside other collections
  private int nestingDepth = 0;
  private final int nestingDepthLimit;

  /**
   * Create
   *
   * @param parser - the parser
   * @param resolver - the resolver
   * @param loadingConfig - options
   */
  public Composer(Parser parser, Resolver resolver, LoaderOptions loadingConfig) {
    if (parser == null) {
      throw new NullPointerException("Parser must be provided");
    }
    if (resolver == null) {
      throw new NullPointerException("Resolver must be provided");
    }
    if (loadingConfig == null) {
      throw new NullPointerException("LoaderOptions must be provided");
    }
    this.parser = parser;
    this.resolver = resolver;
    this.anchors = new HashMap<>();
    this.recursiveNodes = new HashSet<>();
    this.loadingConfig = loadingConfig;
    this.blockCommentsCollector =
        new CommentEventsCollector(parser, CommentType.BLANK_LINE, CommentType.BLOCK);
    this.inlineCommentsCollector = new CommentEventsCollector(parser, CommentType.IN_LINE);
    nestingDepthLimit = loadingConfig.getNestingDepthLimit();
  }

  /**
   * Checks if further documents are available.
   *
   * @return <code>true</code> if there is at least one more document.
   */
  public boolean checkNode() {
    // Drop the STREAM-START event.
    if (parser.checkEvent(Event.ID.StreamStart)) {
      parser.getEvent();
    }
    // If there are more documents available?
    return !parser.checkEvent(Event.ID.StreamEnd);
  }

  /**
   * Reads and composes the next document.
   *
   * @return The root node of the document or <code>null</code> if no more documents are available.
   */
  public Node getNode() {
    // Collect inter-document start comments
    blockCommentsCollector.collectEvents();
    if (parser.checkEvent(Event.ID.StreamEnd)) {
      List<CommentLine> commentLines = blockCommentsCollector.consume();
      Mark startMark = commentLines.get(0).getStartMark();
      List<NodeTuple> children = Collections.emptyList();
      Node node = new MappingNode(Tag.COMMENT, false, children, startMark, null, FlowStyle.BLOCK);
      node.setBlockComments(commentLines);
      return node;
    }
    // Drop the DOCUMENT-START event.
    parser.getEvent();
    // Compose the root node.
    Node node = composeNode(null);
    // Drop the DOCUMENT-END event.
    blockCommentsCollector.collectEvents();
    if (!blockCommentsCollector.isEmpty()) {
      node.setEndComments(blockCommentsCollector.consume());
    }
    parser.getEvent();
    this.anchors.clear();
    this.recursiveNodes.clear();
    return node;
  }

  /**
   * Reads a document from a source that contains only one document.
   * <p>
   * If the stream contains more than one document an exception is thrown.
   * </p>
   *
   * @return The root node of the document or <code>null</code> if no document is available.
   */
  public Node getSingleNode() {
    // Drop the STREAM-START event.
    parser.getEvent();
    // Compose a document if the stream is not empty.
    Node document = null;
    if (!parser.checkEvent(Event.ID.StreamEnd)) {
      document = getNode();
    }
    // Ensure that the stream contains no more documents.
    if (!parser.checkEvent(Event.ID.StreamEnd)) {
      Event event = parser.getEvent();
      Mark contextMark = document != null ? document.getStartMark() : null;
      throw new ComposerException("expected a single document in the stream", contextMark,
          "but found another document", event.getStartMark());
    }
    // Drop the STREAM-END event.
    parser.getEvent();
    return document;
  }

  private Node composeNode(Node parent) {
    blockCommentsCollector.collectEvents();
    if (parent != null) {
      recursiveNodes.add(parent);
    }
    final Node node;
    if (parser.checkEvent(Event.ID.Alias)) {
      AliasEvent event = (AliasEvent) parser.getEvent();
      String anchor = event.getAnchor();
      if (!anchors.containsKey(anchor)) {
        throw new ComposerException(null, null, "found undefined alias " + anchor,
            event.getStartMark());
      }
      node = anchors.get(anchor);
      if (!(node instanceof ScalarNode)) {
        this.nonScalarAliasesCount++;
        if (this.nonScalarAliasesCount > loadingConfig.getMaxAliasesForCollections()) {
          throw new YAMLException(
              "Number of aliases for non-scalar nodes exceeds the specified max="
                  + loadingConfig.getMaxAliasesForCollections());
        }
      }
      if (recursiveNodes.remove(node)) {
        node.setTwoStepsConstruction(true);
      }
      // drop comments, they can not be supported here
      blockCommentsCollector.consume();
      inlineCommentsCollector.collectEvents().consume();
    } else {
      NodeEvent event = (NodeEvent) parser.peekEvent();
      String anchor = event.getAnchor();
      increaseNestingDepth();
      // the check for duplicate anchors has been removed (issue 174)
      if (parser.checkEvent(Event.ID.Scalar)) {
        node = composeScalarNode(anchor, blockCommentsCollector.consume());
      } else if (parser.checkEvent(Event.ID.SequenceStart)) {
        node = composeSequenceNode(anchor);
      } else {
        node = composeMappingNode(anchor);
      }
      decreaseNestingDepth();
    }
    recursiveNodes.remove(parent);
    return node;
  }

  protected Node composeScalarNode(String anchor, List<CommentLine> blockComments) {
    ScalarEvent ev = (ScalarEvent) parser.getEvent();
    String tag = ev.getTag();
    boolean resolved = false;
    Tag nodeTag;
    if (tag == null || tag.equals("!")) {
      nodeTag = resolver.resolve(NodeId.scalar, ev.getValue(),
          ev.getImplicit().canOmitTagInPlainScalar());
      resolved = true;
    } else {
      nodeTag = new Tag(tag);
      if (nodeTag.isCustomGlobal()
          && !loadingConfig.getTagInspector().isGlobalTagAllowed(nodeTag)) {
        throw new ComposerException(null, null, "Global tag is not allowed: " + tag,
            ev.getStartMark());
      }
    }
    Node node = new ScalarNode(nodeTag, resolved, ev.getValue(), ev.getStartMark(), ev.getEndMark(),
        ev.getScalarStyle());
    if (anchor != null) {
      node.setAnchor(anchor);
      anchors.put(anchor, node);
    }
    node.setBlockComments(blockComments);
    node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
    return node;
  }

  protected Node composeSequenceNode(String anchor) {
    SequenceStartEvent startEvent = (SequenceStartEvent) parser.getEvent();
    String tag = startEvent.getTag();
    Tag nodeTag;

    boolean resolved = false;
    if (tag == null || tag.equals("!")) {
      nodeTag = resolver.resolve(NodeId.sequence, null, startEvent.getImplicit());
      resolved = true;
    } else {
      nodeTag = new Tag(tag);
      if (nodeTag.isCustomGlobal()
          && !loadingConfig.getTagInspector().isGlobalTagAllowed(nodeTag)) {
        throw new ComposerException(null, null, "Global tag is not allowed: " + tag,
            startEvent.getStartMark());
      }
    }
    final ArrayList<Node> children = new ArrayList<>();
    SequenceNode node = new SequenceNode(nodeTag, resolved, children, startEvent.getStartMark(),
        null, startEvent.getFlowStyle());
    if (startEvent.isFlow()) {
      node.setBlockComments(blockCommentsCollector.consume());
    }
    if (anchor != null) {
      node.setAnchor(anchor);
      anchors.put(anchor, node);
    }
    while (!parser.checkEvent(Event.ID.SequenceEnd)) {
      blockCommentsCollector.collectEvents();
      if (parser.checkEvent(Event.ID.SequenceEnd)) {
        break;
      }
      children.add(composeNode(node));
    }
    if (startEvent.isFlow()) {
      node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
    }
    Event endEvent = parser.getEvent();
    node.setEndMark(endEvent.getEndMark());
    inlineCommentsCollector.collectEvents();
    if (!inlineCommentsCollector.isEmpty()) {
      node.setInLineComments(inlineCommentsCollector.consume());
    }
    return node;
  }

  protected Node composeMappingNode(String anchor) {
    MappingStartEvent startEvent = (MappingStartEvent) parser.getEvent();
    String tag = startEvent.getTag();
    Tag nodeTag;
    boolean resolved = false;
    if (tag == null || tag.equals("!")) {
      nodeTag = resolver.resolve(NodeId.mapping, null, startEvent.getImplicit());
      resolved = true;
    } else {
      nodeTag = new Tag(tag);
      if (nodeTag.isCustomGlobal()
          && !loadingConfig.getTagInspector().isGlobalTagAllowed(nodeTag)) {
        throw new ComposerException(null, null, "Global tag is not allowed: " + tag,
            startEvent.getStartMark());
      }
    }

    final List<NodeTuple> children = new ArrayList<>();
    MappingNode node = new MappingNode(nodeTag, resolved, children, startEvent.getStartMark(), null,
        startEvent.getFlowStyle());
    if (startEvent.isFlow()) {
      node.setBlockComments(blockCommentsCollector.consume());
    }
    if (anchor != null) {
      node.setAnchor(anchor);
      anchors.put(anchor, node);
    }
    while (!parser.checkEvent(Event.ID.MappingEnd)) {
      blockCommentsCollector.collectEvents();
      if (parser.checkEvent(Event.ID.MappingEnd)) {
        break;
      }
      composeMappingChildren(children, node);
    }
    if (startEvent.isFlow()) {
      node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
    }
    Event endEvent = parser.getEvent();
    node.setEndMark(endEvent.getEndMark());
    inlineCommentsCollector.collectEvents();
    if (!inlineCommentsCollector.isEmpty()) {
      node.setInLineComments(inlineCommentsCollector.consume());
    }
    return node;
  }

  /**
   * Compose the members of mapping
   *
   * @param children - the data to fill
   * @param node - the source
   */
  protected void composeMappingChildren(List<NodeTuple> children, MappingNode node) {
    Node itemKey = composeKeyNode(node);
    if (itemKey.getTag().equals(Tag.MERGE)) {
      node.setMerged(true);
    }
    Node itemValue = composeValueNode(node);
    children.add(new NodeTuple(itemKey, itemValue));
  }

  /**
   * To be able to override composeNode(node) which is a key
   *
   * @param node - the source
   * @return node
   */
  protected Node composeKeyNode(MappingNode node) {
    return composeNode(node);
  }

  /**
   * To be able to override composeNode(node) which is a value
   *
   * @param node - the source
   * @return node
   */
  protected Node composeValueNode(MappingNode node) {
    return composeNode(node);
  }

  /**
   * Increase nesting depth and fail when it exceeds the denied limit
   */
  private void increaseNestingDepth() {
    if (nestingDepth > nestingDepthLimit) {
      throw new YAMLException("Nesting Depth exceeded max " + nestingDepthLimit);
    }
    nestingDepth++;
  }

  /**
   * Indicate that the collection is finished and the nesting is decreased
   */
  private void decreaseNestingDepth() {
    if (nestingDepth > 0) {
      nestingDepth--;
    } else {
      throw new YAMLException("Nesting Depth cannot be negative");
    }
  }
}
