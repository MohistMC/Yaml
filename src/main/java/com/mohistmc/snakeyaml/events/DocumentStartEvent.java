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

import com.mohistmc.snakeyaml.DumperOptions.Version;
import com.mohistmc.snakeyaml.error.Mark;
import java.util.Map;
import lombok.Getter;

/**
 * Marks the beginning of a document.
 * <p>
 * This event followed by the document's content and a {@link DocumentEndEvent}.
 * </p>
 */
public final class DocumentStartEvent extends com.mohistmc.snakeyaml.events.Event {

    private final boolean explicit;
    /**
     * -- GETTER --
     *  YAML version the document conforms to.
     *
     * @return <code>null</code>if the document has no explicit <code>%YAML</code> directive.
     *         Otherwise an array with two components, the major and minor part of the version (in
     *         this order).
     */
    @Getter
    private final Version version;
    /**
     * -- GETTER --
     *  Tag shorthands as defined by the <code>%TAG</code> directive.
     *
     * @return Mapping of 'handles' to 'prefixes' (the handles include the '!' characters).
     */
    @Getter
    private final Map<String, String> tags;

    /**
     * Create
     *
     * @param startMark - start
     * @param endMark - end
     * @param explicit - true when it is present in the document
     * @param version - YAML version
     * @param tags - tag directives
     */
    public DocumentStartEvent(Mark startMark, Mark endMark, boolean explicit, Version version,
                              Map<String, String> tags) {
        super(startMark, endMark);
        this.explicit = explicit;
        this.version = version;
        this.tags = tags;
    }

    /**
     * getter
     *
     * @return true when document end is present
     */
    public boolean getExplicit() {
        return explicit;
    }

    @Override
    public ID getEventId() {
        return ID.DocumentStart;
    }
}
