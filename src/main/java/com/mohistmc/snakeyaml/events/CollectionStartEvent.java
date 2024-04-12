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
package com.mohistmc.snakeyaml.events;

import com.mohistmc.snakeyaml.DumperOptions;
import com.mohistmc.snakeyaml.error.Mark;

/**
 * Base class for the start events of the collection nodes.
 */
public abstract class CollectionStartEvent extends NodeEvent {

  private final String tag;
  // The implicit flag of a collection start event indicates if the tag may be
  // omitted when the collection is emitted
  private final boolean implicit;
  // flag indicates if a collection is block or flow
  private final DumperOptions.FlowStyle flowStyle;

  /**
   * Create
   *
   * @param anchor - its anchor
   * @param tag - its tag
   * @param implicit - thue when the tag is implicitly resolved
   * @param startMark - start
   * @param endMark - end
   * @param flowStyle - style
   */
  public CollectionStartEvent(String anchor, String tag, boolean implicit, Mark startMark,
      Mark endMark, DumperOptions.FlowStyle flowStyle) {
    super(anchor, startMark, endMark);
    this.tag = tag;
    this.implicit = implicit;
    if (flowStyle == null) {
      throw new NullPointerException("Flow style must be provided.");
    }
    this.flowStyle = flowStyle;
  }

  /**
   * Tag of this collection.
   *
   * @return The tag of this collection, or <code>null</code> if no explicit tag is available.
   */
  public String getTag() {
    return this.tag;
  }

  /**
   * <code>true</code> if the tag can be omitted while this collection is emitted.
   *
   * @return True if the tag can be omitted while this collection is emitted.
   */
  public boolean getImplicit() {
    return this.implicit;
  }

  /**
   * <code>true</code> if this collection is in flow style, <code>false</code> for block style.
   *
   * @return If this collection is in flow style.
   */
  public DumperOptions.FlowStyle getFlowStyle() {
    return this.flowStyle;
  }

  @Override
  protected String getArguments() {
    return super.getArguments() + ", tag=" + tag + ", implicit=" + implicit;
  }

  /**
   * Getter
   *
   * @return true for FLOW (exclude BLOCK and AUTO)
   */
  public boolean isFlow() {
    return DumperOptions.FlowStyle.FLOW == flowStyle;
  }
}
