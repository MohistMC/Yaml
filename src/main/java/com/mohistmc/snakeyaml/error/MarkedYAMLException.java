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
package com.mohistmc.snakeyaml.error;

public class MarkedYAMLException extends YAMLException {

  private static final long serialVersionUID = -9119388488683035101L;
  private final String context;
  private final com.mohistmc.snakeyaml.error.Mark contextMark;
  private final String problem;
  private final com.mohistmc.snakeyaml.error.Mark problemMark;
  private final String note;

  protected MarkedYAMLException(String context, com.mohistmc.snakeyaml.error.Mark contextMark, String problem, com.mohistmc.snakeyaml.error.Mark problemMark,
                                String note) {
    this(context, contextMark, problem, problemMark, note, null);
  }

  protected MarkedYAMLException(String context, com.mohistmc.snakeyaml.error.Mark contextMark, String problem, com.mohistmc.snakeyaml.error.Mark problemMark,
                                String note, Throwable cause) {
    super(context + "; " + problem + "; " + problemMark, cause);
    this.context = context;
    this.contextMark = contextMark;
    this.problem = problem;
    this.problemMark = problemMark;
    this.note = note;
  }

  protected MarkedYAMLException(String context, com.mohistmc.snakeyaml.error.Mark contextMark, String problem,
                                com.mohistmc.snakeyaml.error.Mark problemMark) {
    this(context, contextMark, problem, problemMark, null, null);
  }

  protected MarkedYAMLException(String context, com.mohistmc.snakeyaml.error.Mark contextMark, String problem, com.mohistmc.snakeyaml.error.Mark problemMark,
                                Throwable cause) {
    this(context, contextMark, problem, problemMark, null, cause);
  }

  @Override
  public String getMessage() {
    return toString();
  }

  @Override
  public String toString() {
    StringBuilder lines = new StringBuilder();
    if (context != null) {
      lines.append(context);
      lines.append("\n");
    }
    if (contextMark != null && (problem == null || problemMark == null
        || contextMark.getName().equals(problemMark.getName())
        || (contextMark.getLine() != problemMark.getLine())
        || (contextMark.getColumn() != problemMark.getColumn()))) {
      lines.append(contextMark);
      lines.append("\n");
    }
    if (problem != null) {
      lines.append(problem);
      lines.append("\n");
    }
    if (problemMark != null) {
      lines.append(problemMark);
      lines.append("\n");
    }
    if (note != null) {
      lines.append(note);
      lines.append("\n");
    }
    return lines.toString();
  }

  public String getContext() {
    return context;
  }

  public com.mohistmc.snakeyaml.error.Mark getContextMark() {
    return contextMark;
  }

  public String getProblem() {
    return problem;
  }

  public Mark getProblemMark() {
    return problemMark;
  }
}
