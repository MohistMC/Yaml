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

/**
 * This class is only used during representation (dumping)
 */
public class AnchorNode extends com.mohistmc.snakeyaml.nodes.Node {

  private final com.mohistmc.snakeyaml.nodes.Node realNode;

  /**
   * Anchor
   *
   * @param realNode - the node which contains the referenced data
   */
  public AnchorNode(com.mohistmc.snakeyaml.nodes.Node realNode) {
    super(realNode.getTag(), realNode.getStartMark(), realNode.getEndMark());
    this.realNode = realNode;
  }

  @Override
  public com.mohistmc.snakeyaml.nodes.NodeId getNodeId() {
    return NodeId.anchor;
  }

  /**
   * Getter
   *
   * @return node with data
   */
  public Node getRealNode() {
    return realNode;
  }
}
