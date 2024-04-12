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
package com.mohistmc.snakeyaml.events;

import com.mohistmc.snakeyaml.comments.CommentType;
import com.mohistmc.snakeyaml.error.Mark;
import lombok.Getter;

/**
 * Marks a comment block value.
 */
public final class CommentEvent extends com.mohistmc.snakeyaml.events.Event {

    private final CommentType type;
    /**
     * -- GETTER --
     *  String representation of the value.
     *  <p>
     *  Without quotes and escaping.
     *  </p>
     *
     * @return Value a comment line string without the leading '#' or a blank line.
     */
    @Getter
    private final String value;

    /**
     * Create
     *
     * @param type - kind
     * @param value - text
     * @param startMark - start
     * @param endMark - end
     */
    public CommentEvent(CommentType type, String value, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        if (type == null) {
            throw new NullPointerException("Event Type must be provided.");
        }
        this.type = type;
        if (value == null) {
            throw new NullPointerException("Value must be provided.");
        }
        this.value = value;
    }

    /**
     * The comment type.
     *
     * @return the commentType.
     */
    public CommentType getCommentType() {
        return this.type;
    }

    @Override
    protected String getArguments() {
        return super.getArguments() + "type=" + type + ", value=" + value;
    }

    @Override
    public ID getEventId() {
        return ID.Comment;
    }

}
