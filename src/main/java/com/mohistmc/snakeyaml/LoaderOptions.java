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

import com.mohistmc.snakeyaml.inspector.TagInspector;
import com.mohistmc.snakeyaml.inspector.UnTrustedTagInspector;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for loading
 */
public class LoaderOptions {

    /**
     * -- SETTER --
     *  Allow/Reject duplicate map keys in the YAML file.
     *  Default is to allow.
     *  YAML 1.1 is slightly vague around duplicate entries in the YAML file. The best reference is
     *  <a href="http://www.yaml.org/spec/1.1/#id862121"> 3.2.1.3. Nodes Comparison</a> where it hints
     *  that a duplicate map key is an error.
     *  For future reference, YAML spec 1.2 is clear. The keys MUST be unique.
     *  <a href="http://www.yaml.org/spec/1.2/spec.html#id2759572">1.3. Relation to JSON</a>
     *
     * @param allowDuplicateKeys false to reject duplicate mapping keys
     */
    @Setter
    private boolean allowDuplicateKeys = true;

    /**
     * -- SETTER --
     *  Wrap runtime exception to YAMLException during parsing or leave them as they are
     *  Default is to leave original exceptions
     *
     * @param wrappedToRootException - true to convert runtime exception to YAMLException
     */
    @Setter
    private boolean wrappedToRootException = false;

    /**
     * -- SETTER --
     *  Restrict the amount of aliases for collections (sequences and mappings) to avoid
     *  https://en.wikipedia.org/wiki/Billion_laughs_attack
     *
     * @param maxAliasesForCollections set max allowed value (50 by default)
     */
    @Setter
    private int maxAliasesForCollections = 50; // to prevent YAML at

    /**
     * -- SETTER --
     *  Allow recursive keys for mappings. By default, it is not allowed. This setting only prevents
     *  the case when the key is the value. If the key is only a part of the value (the value is a
     *  sequence or a mapping) then this case is not recognized and always allowed.
     *
     * @param allowRecursiveKeys - false to disable recursive keys
     */
    // https://en.wikipedia.org/wiki/Billion_laughs_attack
    @Setter
    private boolean allowRecursiveKeys = false;

    private boolean processComments = false;

    /**
     * -- SETTER --
     *  Disables or enables case sensitivity during construct enum constant from string value Default
     *  is false.
     *
     * @param enumCaseSensitive - true to set enum case-sensitive, false the reverse
     */
    @Setter
    private boolean enumCaseSensitive = true;

    /**
     * -- SETTER --
     *  Set max depth of nested collections. When the limit is exceeded an exception is thrown.
     *  Aliases/Anchors are not counted. This is to prevent a DoS attack
     *
     * @param nestingDepthLimit - depth to be accepted (50 by default)
     */
    @Setter
    private int nestingDepthLimit = 50;

    /**
     * -- SETTER --
     *  The max amount of code points for every input YAML document in the stream. Please be aware that
     *  byte limit depends on the encoding.
     *
     * @param codePointLimit - the max allowed size of a single YAML document in a stream
     */
    @Setter
    private int codePointLimit = 3 * 1024 * 1024; // 3 MB

    /**
     * Secure by default - no custom classes are allowed
     */
    @Setter
    @Getter
    private TagInspector tagInspector = new UnTrustedTagInspector();

    /**
     * getter
     *
     * @return true when duplicate keys in mapping allowed (the latter overrides the former)
     */
    public final boolean isAllowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    /**
     * getter
     *
     * @return true when wrapped
     */
    public final boolean isWrappedToRootException() {
        return wrappedToRootException;
    }

    /**
     * getter
     *
     * @return show the limit
     */
    public final int getMaxAliasesForCollections() {
        return maxAliasesForCollections;
    }

    /**
     * getter
     *
     * @return when recursive keys are allowed (the document should be trusted)
     */
    public final boolean getAllowRecursiveKeys() {
        return allowRecursiveKeys;
    }

    /**
     * getter
     *
     * @return comments are kept in Node
     */
    public final boolean isProcessComments() {
        return processComments;
    }

    /**
     * Set the comment processing. By default, comments are ignored.
     *
     * @param processComments <code>true</code> to process; <code>false</code> to ignore
     * @return applied options
     */
    public LoaderOptions setProcessComments(boolean processComments) {
        this.processComments = processComments;
        return this;
    }

    /**
     * getter
     *
     * @return true when parsing enum case-sensitive
     */
    public final boolean isEnumCaseSensitive() {
        return enumCaseSensitive;
    }

    /**
     * getter
     *
     * @return the limit
     */
    public final int getNestingDepthLimit() {
        return nestingDepthLimit;
    }

    /**
     * getter
     *
     * @return max code points in the input document
     */
    public final int getCodePointLimit() {
        return codePointLimit;
    }

}
