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
package com.mohistmc.snakeyaml.error;

import com.mohistmc.snakeyaml.scanner.Constant;
import java.io.Serializable;
import lombok.Getter;

/**
 * It's just a record and its only use is producing nice error messages. Parser does not use it for
 * any other purposes.
 */
@Getter
public final class Mark implements Serializable {

    private final String name;
    /**
     * -- GETTER --
     *  starts with 0
     *
     * @return character number
     */
    private final int index;
    /**
     * -- GETTER --
     *  starts with 0
     *
     * @return line number
     */
    private final int line;
    /**
     * -- GETTER --
     *  starts with 0
     *
     * @return column number
     */
    private final int column;
    private final int[] buffer;
    private final int pointer;

    public Mark(String name, int index, int line, int column, char[] str, int pointer) {
        this(name, index, line, column, toCodePoints(str), pointer);
    }

    public Mark(String name, int index, int line, int column, int[] buffer, int pointer) {
        super();
        this.name = name;
        this.index = index;
        this.line = line;
        this.column = column;
        this.buffer = buffer;
        this.pointer = pointer;
    }

    private static int[] toCodePoints(char[] str) {
        int[] codePoints = new int[Character.codePointCount(str, 0, str.length)];
        for (int i = 0, c = 0; i < str.length; c++) {
            int cp = Character.codePointAt(str, i);
            codePoints[c] = cp;
            i += Character.charCount(cp);
        }
        return codePoints;
    }

    private boolean isLineBreak(int c) {
        return Constant.NULL_OR_LINEBR.has(c);
    }

    public String get_snippet(int indent, int max_length) {
        float half = max_length / 2f - 1f;
        int start = pointer;
        String head = "";
        while ((start > 0) && !isLineBreak(buffer[start - 1])) {
            start -= 1;
            if (pointer - start > half) {
                head = " ... ";
                start += 5;
                break;
            }
        }
        String tail = "";
        int end = pointer;
        while ((end < buffer.length) && !isLineBreak(buffer[end])) {
            end += 1;
            if (end - pointer > half) {
                tail = " ... ";
                end -= 5;
                break;
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(" ".repeat(Math.max(0, indent)));
        result.append(head);
        for (int i = start; i < end; i++) {
            result.appendCodePoint(buffer[i]);
        }
        result.append(tail);
        result.append("\n");
        result.append(" ".repeat(Math.max(0, indent + pointer - start + head.length())));
        result.append("^");
        return result.toString();
    }

    public String get_snippet() {
        return get_snippet(4, 75);
    }

    @Override
    public String toString() {
        String snippet = get_snippet();
        return " in " + name + ", line " + (line + 1) + ", column " + (column + 1) + ":\n" + snippet;
    }

}
