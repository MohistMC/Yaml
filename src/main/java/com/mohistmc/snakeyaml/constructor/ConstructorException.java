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
package com.mohistmc.snakeyaml.constructor;

import com.mohistmc.snakeyaml.error.Mark;
import com.mohistmc.snakeyaml.error.MarkedYAMLException;
import java.io.Serial;

/**
 * Exception during object construction
 */
public class ConstructorException extends MarkedYAMLException {

    @Serial
    private static final long serialVersionUID = -8816339931365239910L;

    /**
     * Create
     *
     * @param context - part of the document
     * @param contextMark - context position
     * @param problem - the issue
     * @param problemMark - problem position
     * @param cause - the reason
     */
    protected ConstructorException(String context, Mark contextMark, String problem, Mark problemMark,
                                   Throwable cause) {
        super(context, contextMark, problem, problemMark, cause);
    }

    /**
     * Create
     *
     * @param context - part of the document
     * @param contextMark - context position
     * @param problem - the issue
     * @param problemMark - problem position
     */
    protected ConstructorException(String context, Mark contextMark, String problem,
                                   Mark problemMark) {
        this(context, contextMark, problem, problemMark, null);
    }
}
