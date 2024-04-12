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
package com.mohistmc.snakeyaml.comments;

import com.mohistmc.snakeyaml.error.Mark;
import com.mohistmc.snakeyaml.events.CommentEvent;
import lombok.Getter;

/**
 * A comment line. It may be a block comment, blank line, or inline comment.
 */
@Getter
public class CommentLine {

    /**
     * -- GETTER --
     *  getter
     *
     * @return start
     */
    private final Mark startMark;
    /**
     * -- GETTER --
     *  getter
     *
     * @return end
     */
    private final Mark endMark;
    /**
     * -- GETTER --
     *  Value of this comment.
     *
     * @return comment's value.
     */
    private final String value;
    /**
     * -- GETTER --
     *  Getter
     *
     * @return kind
     */
    private final CommentType commentType;

    /**
     * Create
     *
     * @param event - the source
     */
    public CommentLine(CommentEvent event) {
        this(event.getStartMark(), event.getEndMark(), event.getValue(), event.getCommentType());
    }

    /**
     * Create
     *
     * @param startMark - start
     * @param endMark - end
     * @param value - text
     * @param commentType - kind
     */
    public CommentLine(Mark startMark, Mark endMark, String value, CommentType commentType) {
        this.startMark = startMark;
        this.endMark = endMark;
        this.value = value;
        this.commentType = commentType;
    }

    public String toString() {
        return "<" + this.getClass().getName() + " (type=" + getCommentType() + ", value=" + getValue()
                + ")>";
    }
}
