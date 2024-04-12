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
package com.mohistmc.snakeyaml;

import com.mohistmc.snakeyaml.emitter.Emitter;
import com.mohistmc.snakeyaml.error.YAMLException;
import com.mohistmc.snakeyaml.serializer.AnchorGenerator;
import com.mohistmc.snakeyaml.serializer.NumberAnchorGenerator;
import java.util.Map;
import java.util.TimeZone;
import lombok.Getter;
import lombok.Setter;


/**
 * Configuration for serialisation
 */
public class DumperOptions {

    private ScalarStyle defaultStyle = ScalarStyle.PLAIN;
    /**
     * -- GETTER --
     *  getter
     *
     * @return flow style for collections
     */
    @Getter
    private FlowStyle defaultFlowStyle = FlowStyle.AUTO;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  Force the emitter to produce a canonical YAML document.
     *
     @return true when well established format should be dumped
      * @param canonical true produce canonical YAML document
     */
    @Setter
    @Getter
    private boolean canonical = false;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  Specify whether to emit non-ASCII printable Unicode characters. The default value is true. When
     *  set to false then printable non-ASCII characters (Cyrillic, Chinese etc) will be not printed
     *  but escaped (to support ASCII terminals)
     *
     @return false when non-ASCII is escaped
      * @param allowUnicode if allowUnicode is false then all non-ASCII characters are escaped
     */
    @Setter
    @Getter
    private boolean allowUnicode = true;
    /**
     * -- GETTER --
     *  Report whether read-only JavaBean properties (the ones without setters) should be included in
     *  the YAML document
     *
     *
     * -- SETTER --
     *  Set to true to include read-only JavaBean properties (the ones without setters) in the YAML
     *  document. By default these properties are not included to be able to parse later the same
     *  JavaBean.
     *
     @return false when read-only JavaBean properties are not emitted
      * @param allowReadOnlyProperties - true to dump read-only JavaBean properties
     */
    @Setter
    @Getter
    private boolean allowReadOnlyProperties = false;
    /**
     * -- GETTER --
     *  getter
     *
     * @return indent
     */
    @Getter
    private int indent = 2;
    @Getter
    private int indicatorIndent = 0;
    /**
     * -- SETTER --
     *  Set to true to add the indent for sequences to the general indent
     *
     * @param indentWithIndicator - true when indent for sequences is added to general
     */
    @Setter
    private boolean indentWithIndicator = false;
    private int bestWidth = 80;
    /**
     * -- SETTER --
     *  Specify whether to split lines exceeding preferred width for scalars. The default is true.
     *
     * @param splitLines whether to split lines exceeding preferred width for scalars.
     */
    @Setter
    private boolean splitLines = true;
    /**
     * -- GETTER --
     *  getter
     *
     * @return line break to separate lines
     */
    @Getter
    private LineBreak lineBreak = LineBreak.UNIX;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  setter - require explicit '...'
     *
     @return true when '---' must be printed
      * @param explicitStart - true to emit '---'
     */
    @Setter
    @Getter
    private boolean explicitStart = false;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  setter - require explicit '...'
     *
     @return true when '...' must be printed
      * @param explicitEnd - true to emit '...'
     */
    @Setter
    @Getter
    private boolean explicitEnd = false;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  Set the timezone to be used for Date. If set to <code>null</code> UTC is used.
     *
     @return timezone to be used to emit Date
      * @param timeZone for created Dates or null to use UTC
     */
    @Setter
    @Getter
    private TimeZone timeZone = null;
    @Getter
    private int maxSimpleKeyLength = 128;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  Set the comment processing. By default, comments are ignored.
     *
     @return true when comments are not ignored and can be used after composing a Node
      * @param processComments <code>true</code> to process; <code>false</code> to ignore
     */
    @Setter
    @Getter
    private boolean processComments = false;
    /**
     * -- SETTER --
     *  When String contains non-printable characters SnakeYAML convert it to binary data with the
     *  !!binary tag. Set this to ESCAPE to keep the !!str tag and escape the non-printable chars with
     *  \\x or \\u
     *
     * @param style ESCAPE to force SnakeYAML to keep !!str tag for non-printable data
     */
    @Setter
    @Getter
    private NonPrintableStyle nonPrintableStyle = NonPrintableStyle.BINARY;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  Of no use - it is better not to include YAML version as the directive
     *
     @return the expected version
      * @param version 1.0 or 1.1
     */
    @Setter
    @Getter
    private Version version = null;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  setter
     *
     @return previously defined tag directives
      * @param tags - tag directives for the YAML document
     */
    @Setter
    @Getter
    private Map<String, String> tags = null;
    private Boolean prettyFlow = false;
    /**
     * -- GETTER --
     *  getter
     *
     *
     * -- SETTER --
     *  Provide a custom generator
     *
     @return generator to create anchor names
      * @param anchorGenerator - the way to create custom anchors
     */
    @Setter
    @Getter
    private AnchorGenerator anchorGenerator = new NumberAnchorGenerator(0);
    /**
     * -- SETTER --
     *  Forces Serializer to skip emitting Anchors names, emit Node content instead of Alias, fail with
     *  SerializationException if serialized structure is recursive.
     *  Default value is <code>false</code> - emit Aliases.
     *
     * @param dereferenceAliases emit node referenced by the alias or alias itself
     */
    @Setter
    @Getter
    private boolean dereferenceAliases = false;

    /**
     * getter
     *
     * @return scalar style
     */
    public ScalarStyle getDefaultScalarStyle() {
        return defaultStyle;
    }

    /**
     * Set default style for scalars. See YAML 1.1 specification, 2.3 Scalars
     * (http://yaml.org/spec/1.1/#id858081)
     *
     * @param defaultStyle set the style for all scalars
     */
    public void setDefaultScalarStyle(ScalarStyle defaultStyle) {
        if (defaultStyle == null) {
            throw new NullPointerException("Use ScalarStyle enum.");
        }
        this.defaultStyle = defaultStyle;
    }

    /**
     * Define indentation. Must be within the limits (1-10)
     *
     * @param indent number of spaces to serve as indentation
     */
    public void setIndent(int indent) {
        if (indent < Emitter.MIN_INDENT) {
            throw new YAMLException("Indent must be at least " + Emitter.MIN_INDENT);
        }
        if (indent > Emitter.MAX_INDENT) {
            throw new YAMLException("Indent must be at most " + Emitter.MAX_INDENT);
        }
        this.indent = indent;
    }

    /**
     * Set number of white spaces to use for the sequence indicator '-'
     *
     * @param indicatorIndent value to be used as indent
     */
    public void setIndicatorIndent(int indicatorIndent) {
        if (indicatorIndent < 0) {
            throw new YAMLException("Indicator indent must be non-negative.");
        }
        if (indicatorIndent > Emitter.MAX_INDENT - 1) {
            throw new YAMLException(
                    "Indicator indent must be at most Emitter.MAX_INDENT-1: " + (Emitter.MAX_INDENT - 1));
        }
        this.indicatorIndent = indicatorIndent;
    }

    public boolean getIndentWithIndicator() {
        return indentWithIndicator;
    }

    /**
     * getter
     *
     * @return true for pretty style
     */
    public boolean isPrettyFlow() {
        return this.prettyFlow;
    }

    /**
     * Force the emitter to produce a pretty YAML document when using the flow style.
     *
     * @param prettyFlow true produce pretty flow YAML document
     */
    public void setPrettyFlow(boolean prettyFlow) {
        this.prettyFlow = prettyFlow;
    }

    /**
     * getter
     *
     * @return the preferred width for scalars
     */
    public int getWidth() {
        return this.bestWidth;
    }

    /**
     * Specify the preferred width to emit scalars. When the scalar representation takes more then the
     * preferred with the scalar will be split into a few lines. The default is 80.
     *
     * @param bestWidth the preferred width for scalars.
     */
    public void setWidth(int bestWidth) {
        this.bestWidth = bestWidth;
    }

    /**
     * getter
     *
     * @return true when to split lines exceeding preferred width for scalars
     */
    public boolean getSplitLines() {
        return this.splitLines;
    }

    /**
     * setter
     *
     * @param defaultFlowStyle - enum for the flow style
     */
    public void setDefaultFlowStyle(FlowStyle defaultFlowStyle) {
        if (defaultFlowStyle == null) {
            throw new NullPointerException("Use FlowStyle enum.");
        }
        this.defaultFlowStyle = defaultFlowStyle;
    }

    /**
     * Specify the line break to separate the lines. It is platform specific: Windows - "\r\n", old
     * MacOS - "\r", Unix - "\n". The default value is the one for Unix.
     *
     * @param lineBreak to be used for the input
     */
    public void setLineBreak(LineBreak lineBreak) {
        if (lineBreak == null) {
            throw new NullPointerException("Specify line break.");
        }
        this.lineBreak = lineBreak;
    }

    /**
     * Define max key length to use simple key (without '?') More info
     * https://yaml.org/spec/1.1/#id934537
     *
     * @param maxSimpleKeyLength - the limit after which the key gets explicit key indicator '?'
     */
    public void setMaxSimpleKeyLength(int maxSimpleKeyLength) {
        if (maxSimpleKeyLength > 1024) {
            throw new YAMLException(
                    "The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.1/#id934537");
        }
        this.maxSimpleKeyLength = maxSimpleKeyLength;
    }

    /**
     * YAML provides a rich set of scalar styles. Block scalar styles include the literal style and
     * the folded style; flow scalar styles include the plain style and two quoted styles, the
     * single-quoted style and the double-quoted style. These styles offer a range of trade-offs
     * between expressive power and readability.
     *
     * @see <a href="http://yaml.org/spec/1.1/#id903915">Chapter 9. Scalar Styles</a>
     * @see <a href="http://yaml.org/spec/1.1/#id858081">2.3. Scalars</a>
     */
    public enum ScalarStyle {
        /**
         * Double quoted scalar
         */
        DOUBLE_QUOTED('"'),
        /**
         * Single quoted scalar
         */
        SINGLE_QUOTED('\''),
        /**
         * Literal scalar
         */
        LITERAL('|'),
        /**
         * Folded scalar
         */
        FOLDED('>'),
        /**
         * Plain scalar
         */
        PLAIN(null);

        private final Character styleChar;

        ScalarStyle(Character style) {
            this.styleChar = style;
        }

        /**
         * Create
         *
         * @param style - source char
         * @return parsed style
         */
        public static ScalarStyle createStyle(Character style) {
            if (style == null) {
                return PLAIN;
            } else {
                return switch (style) {
                    case '"' -> DOUBLE_QUOTED;
                    case '\'' -> SINGLE_QUOTED;
                    case '|' -> LITERAL;
                    case '>' -> FOLDED;
                    default -> throw new YAMLException("Unknown scalar style character: " + style);
                };
            }
        }

        /**
         * getter
         *
         * @return the char behind the style
         */
        public Character getChar() {
            return styleChar;
        }

        /**
         * Readable style
         *
         * @return for humans
         */
        @Override
        public String toString() {
            return "Scalar style: '" + styleChar + "'";
        }
    }

    /**
     * Block styles use indentation to denote nesting and scope within the document. In contrast, flow
     * styles rely on explicit indicators to denote nesting and scope.
     *
     * @see <a href="http://www.yaml.org/spec/current.html#id2509255">3.2.3.1. Node Styles
     *      (http://yaml.org/spec/1.1)</a>
     */
    public enum FlowStyle {
        /**
         * Flow style
         */
        FLOW(Boolean.TRUE),
        /**
         * Block style
         */
        BLOCK(Boolean.FALSE),
        /**
         * Auto (first block, then flow)
         */
        AUTO(null);

        private final Boolean styleBoolean;

        FlowStyle(Boolean flowStyle) {
            styleBoolean = flowStyle;
        }

        @Override
        public String toString() {
            return "Flow style: '" + styleBoolean + "'";
        }
    }

    /**
     * Platform dependent line break.
     */
    public enum LineBreak {
        /**
         * Windows
         */
        WIN("\r\n"),
        /**
         * Old Mac (should not be used !)
         */
        MAC("\r"),
        /**
         * Linux and Mac
         */
        UNIX("\n");

        private final String lineBreak;

        /**
         * Create
         *
         * @param lineBreak - break
         */
        LineBreak(String lineBreak) {
            this.lineBreak = lineBreak;
        }

        /**
         * Get the line break used by the current Operating System
         *
         * @return detected line break
         */
        public static LineBreak getPlatformLineBreak() {
            String platformLineBreak = System.lineSeparator();
            for (LineBreak lb : values()) {
                if (lb.lineBreak.equals(platformLineBreak)) {
                    return lb;
                }
            }
            return LineBreak.UNIX;
        }

        /**
         * getter
         *
         * @return the break
         */
        public String getString() {
            return lineBreak;
        }

        /**
         * for humans
         *
         * @return representation
         */
        @Override
        public String toString() {
            return "Line break: " + name();
        }
    }

    /**
     * Specification version. Currently supported 1.0 and 1.1
     */
    public enum Version {
        /**
         * 1.0
         */
        V1_0(new Integer[]{1, 0}),
        /**
         * 1.1
         */
        V1_1(new Integer[]{1, 1});

        private final Integer[] version;

        /**
         * Create
         *
         * @param version - definition
         */
        Version(Integer[] version) {
            this.version = version;
        }

        /**
         * getter
         *
         * @return major part (always 1)
         */
        public int major() {
            return version[0];
        }

        /**
         * Minor part (0 or 1)
         *
         * @return 0 or 1
         */
        public int minor() {
            return version[1];
        }

        /**
         * getter
         *
         * @return representation for serialisation
         */
        public String getRepresentation() {
            return version[0] + "." + version[1];
        }

        /**
         * Readable string
         *
         * @return for humans
         */
        @Override
        public String toString() {
            return "Version: " + getRepresentation();
        }
    }


    /**
     * the way to serialize non-printable
     */
    public enum NonPrintableStyle {
        /**
         * Transform String to binary if it contains non-printable characters
         */
        BINARY,
        /**
         * Escape non-printable characters
         */
        ESCAPE
    }

}
