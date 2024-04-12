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
package com.mohistmc.snakeyaml.composer;

import com.mohistmc.snakeyaml.error.Mark;
import com.mohistmc.snakeyaml.error.MarkedYAMLException;
import java.io.Serial;

/**
 * Exception during compose phase
 */
public class ComposerException extends MarkedYAMLException {

    @Serial
    private static final long serialVersionUID = 2146314636913113935L;

    /**
     * Create
     *
     * @param context - context
     * @param contextMark - mark
     * @param problem - the issue
     * @param problemMark - where the issue occurs
     */
    protected ComposerException(String context, Mark contextMark, String problem, Mark problemMark) {
        super(context, contextMark, problem, problemMark);
    }
}
