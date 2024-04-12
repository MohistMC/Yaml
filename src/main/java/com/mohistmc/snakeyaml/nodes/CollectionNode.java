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
 * Base class for the two collection types {@link MappingNode mapping} and {@link SequenceNode
 * collection}.
 */
@Getter
public abstract class CollectionNode<T> extends Node {

    /**
     * -- GETTER --
     *  Serialization style of this collection.
     *
     * @return <code>true</code> for flow style, <code>false</code> for block style.
     */
    private DumperOptions.FlowStyle flowStyle;

    /**
     * Create
     *
     * @param tag - its tag
     * @param startMark - start
     * @param endMark - end
     * @param flowStyle - style
     */
    public CollectionNode(Tag tag, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle) {
        super(tag, startMark, endMark);
        setFlowStyle(flowStyle);
    }

    /**
     * Returns the elements in this sequence.
     *
     * @return Nodes in the specified order.
     */
    public abstract List<T> getValue();

    /**
     * Setter
     *
     * @param flowStyle - flow style for collections
     */
    public void setFlowStyle(DumperOptions.FlowStyle flowStyle) {
        if (flowStyle == null) {
            throw new NullPointerException("Flow style must be provided.");
        }
        this.flowStyle = flowStyle;
    }

    /**
     * Setter
     *
     * @param endMark - end
     */
    public void setEndMark(Mark endMark) {
        this.endMark = endMark;
    }
}
