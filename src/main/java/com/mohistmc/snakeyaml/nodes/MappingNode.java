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
package com.mohistmc.snakeyaml.nodes;

import com.mohistmc.snakeyaml.DumperOptions;
import com.mohistmc.snakeyaml.error.Mark;
import java.util.List;

/**
 * Represents a map.
 * <p>
 * A map is a collection of unsorted key-value pairs.
 * </p>
 */
public class MappingNode extends CollectionNode<com.mohistmc.snakeyaml.nodes.NodeTuple> {

  private List<com.mohistmc.snakeyaml.nodes.NodeTuple> value;
  private boolean merged = false;

  public MappingNode(com.mohistmc.snakeyaml.nodes.Tag tag, boolean resolved, List<com.mohistmc.snakeyaml.nodes.NodeTuple> value, Mark startMark, Mark endMark,
                     DumperOptions.FlowStyle flowStyle) {
    super(tag, startMark, endMark, flowStyle);
    if (value == null) {
      throw new NullPointerException("value in a Node is required.");
    }
    this.value = value;
    this.resolved = resolved;
  }

  public MappingNode(Tag tag, List<com.mohistmc.snakeyaml.nodes.NodeTuple> value, DumperOptions.FlowStyle flowStyle) {
    this(tag, true, value, null, null, flowStyle);
  }

  @Override
  public com.mohistmc.snakeyaml.nodes.NodeId getNodeId() {
    return NodeId.mapping;
  }

  /**
   * Returns the entries of this map.
   *
   * @return List of entries.
   */
  public List<com.mohistmc.snakeyaml.nodes.NodeTuple> getValue() {
    return value;
  }

  public void setValue(List<com.mohistmc.snakeyaml.nodes.NodeTuple> mergedValue) {
    value = mergedValue;
  }

  public void setOnlyKeyType(Class<? extends Object> keyType) {
    for (com.mohistmc.snakeyaml.nodes.NodeTuple nodes : value) {
      nodes.keyNode().setType(keyType);
    }
  }

  public void setTypes(Class<? extends Object> keyType, Class<? extends Object> valueType) {
    for (com.mohistmc.snakeyaml.nodes.NodeTuple nodes : value) {
      nodes.valueNode().setType(valueType);
      nodes.keyNode().setType(keyType);
    }
  }

  @Override
  public String toString() {
    String values;
    StringBuilder buf = new StringBuilder();
    for (NodeTuple node : getValue()) {
      buf.append("{ key=");
      buf.append(node.keyNode());
      buf.append("; value=");
      if (node.valueNode() instanceof CollectionNode) {
        // to avoid overflow in case of recursive structures
        buf.append(System.identityHashCode(node.valueNode()));
      } else {
        buf.append(node);
      }
      buf.append(" }");
    }
    values = buf.toString();
    return "<" + this.getClass().getName() + " (tag=" + getTag() + ", values=" + values + ")>";
  }

  /**
   * @param merged - true if map contains merge node
   */
  public void setMerged(boolean merged) {
    this.merged = merged;
  }

  /**
   * @return true if map contains merge node
   */
  public boolean isMerged() {
    return merged;
  }
}