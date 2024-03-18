/*
 *  Copyright Osiris Team
 *  All rights reserved.
 *
 *  This software is licensed work.
 *  Please consult the file "LICENSE" for details.
 */

package com.mohistmc.yml;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single line of a yaml file.
 * It gets filled with information by {@link YamlReader#checkChar(DYLine, int, int)} and {@link YamlReader#parseLine(Yaml, DYLine)}.
 */
@Setter
@Getter
public class DYLine {
    private String fullLine;
    private int lineNumber;
    private char[] fullLineAsChar;
    private int countSpaces;
    private boolean commentFound;
    private boolean keyFound;
    private int keyFoundPos;
    private boolean hyphenFound;
    private int hyphenFoundPos;
    private boolean charFound;
    private String rawKey;
    private String rawValue;
    private String rawComment;

    public DYLine(String fullLine, int lineNumber) {
        this.fullLine = fullLine;
        this.lineNumber = lineNumber;
        this.fullLineAsChar = fullLine.toCharArray();
    }

}
