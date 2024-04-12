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
package com.mohistmc.snakeyaml.nodes;

import com.mohistmc.snakeyaml.DumperOptions;
import com.mohistmc.snakeyaml.error.Mark;
import java.util.List;
import lombok.Getter;

/**
 * Represents a sequence.
 * <p>
 * A sequence is a ordered collection of nodes.
 * </p>
 */
@Getter
public class SequenceNode extends CollectionNode<com.mohistmc.snakeyaml.nodes.Node> {

    /**
     * -- GETTER --
     *  Returns the elements in this sequence.
     *
     * @return Nodes in the specified order.
     */
    private final List<com.mohistmc.snakeyaml.nodes.Node> value;

    public SequenceNode(com.mohistmc.snakeyaml.nodes.Tag tag, boolean resolved, List<com.mohistmc.snakeyaml.nodes.Node> value, Mark startMark, Mark endMark,
                        DumperOptions.FlowStyle flowStyle) {
        super(tag, startMark, endMark, flowStyle);
        if (value == null) {
            throw new NullPointerException("value in a Node is required.");
        }
        this.value = value;
        this.resolved = resolved;
    }

    public SequenceNode(Tag tag, List<com.mohistmc.snakeyaml.nodes.Node> value, DumperOptions.FlowStyle flowStyle) {
        this(tag, true, value, null, null, flowStyle);
    }

    @Override
    public com.mohistmc.snakeyaml.nodes.NodeId getNodeId() {
        return NodeId.sequence;
    }

    public void setListType(Class<? extends Object> listType) {
        for (com.mohistmc.snakeyaml.nodes.Node node : value) {
            node.setType(listType);
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Node node : getValue()) {
            if (node instanceof CollectionNode) {
                // to avoid overflow in case of recursive structures
                buf.append(System.identityHashCode(node));
            } else {
                buf.append(node.toString());
            }
            buf.append(",");
        }
        // delete last comma
        if (!buf.isEmpty()) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return "<" + this.getClass().getName() + " (tag=" + getTag() + ", value=[" + buf + "])>";
    }
}
