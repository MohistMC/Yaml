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

import com.mohistmc.snakeyaml.error.Mark;

/**
 * Marks the end of a document.
 * <p>
 * This event follows the document's content.
 * </p>
 */
public final class DocumentEndEvent extends com.mohistmc.snakeyaml.events.Event {

    private final boolean explicit;

    /**
     * Create
     *
     * @param startMark - start
     * @param endMark - end
     * @param explicit - true when it is present in the document, false for implicitly added
     */
    public DocumentEndEvent(Mark startMark, Mark endMark, boolean explicit) {
        super(startMark, endMark);
        this.explicit = explicit;
    }

    /**
     * getter
     *
     * @return true when document end is present in the document
     */
    public boolean getExplicit() {
        return explicit;
    }

    /**
     * getter
     *
     * @return its identity
     */
    @Override
    public ID getEventId() {
        return ID.DocumentEnd;
    }
}
