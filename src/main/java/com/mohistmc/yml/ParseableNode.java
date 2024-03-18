package com.mohistmc.yml;

import java.util.List;

/**
 * Provides all the information necessary
 * to parse a {@link Yaml} object.
 */
public interface ParseableNode {
    String getKey();

    List<String> getValues();

    ParseableNode getParent();

    List<ParseableNode> getChildren();

    String toString(List<ParseableNode> nodes);

    <T extends ParseableNode> List<T> toNodesList(String s);
}
