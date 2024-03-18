/*
 *  Copyright Osiris Team
 *  All rights reserved.
 *
 *  This software is licensed work.
 *  Please consult the file "LICENSE" for details.
 */

package com.mohistmc.yml;

import com.mohistmc.yml.exceptions.YamlWriterException;
import com.mohistmc.yml.utils.BufferedSBWriter;
import com.mohistmc.yml.utils.UtilsTimeStopper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;

/**
 * Responsible for parsing and writing the provided modules.
 */
class YamlWriter {
    private Yaml yaml;

    public void parse(Yaml yaml, boolean overwrite, boolean reset) throws YamlWriterException, IOException {
        this.yaml = yaml;

        UtilsTimeStopper timer = new UtilsTimeStopper();
        timer.start();

        BufferedWriter writer = null;
        if (yaml.outputStream != null) {
            writer = new BufferedWriter(new OutputStreamWriter(yaml.outputStream, Yaml.charset), 32768); // TODO compare speed with def buffer

        } else if (yaml.file != null) {
            if (!yaml.file.exists())
                throw new YamlWriterException("File '" + yaml.file.getName() + "' doesn't exist!");
            writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(yaml.file.toPath()), Yaml.charset), 32768); // TODO compare speed with def buffer

        } else if (yaml.outString != null) {
            writer = new BufferedSBWriter();
        }

        if (writer == null) {
            return;
        }

        try {
            writer.write(""); // Clear old content
            if (reset) return;

            List<YamlSection> sectionsToSave;
            if (overwrite) {
                sectionsToSave = yaml.getAllInEdit();

            } else
                sectionsToSave = yaml.createUnifiedList(yaml.getAllInEdit(), yaml.getAllLoaded());


            YamlSection lastSection = new YamlSection(yaml); // Create an empty module as start point
            for (YamlSection m :
                    sectionsToSave) {
                parseSection(writer, m, lastSection);
                lastSection = m;
            }

            timer.stop();

            if (writer instanceof BufferedSBWriter) {
                yaml.outString = ((BufferedSBWriter) writer).builder.toString();
            }
        } catch (IOException e) {
            if (yaml.file != null || yaml.outString != null) writer.close();
            throw e;
        } finally {
            if (yaml.file != null || yaml.outString != null) writer.close();
        }
    }

    /**
     * Writes an in-memory {@link YamlSection} object to file.
     *
     * @param writer       the writer to use.
     * @param section      the current module to write.
     * @param beforeModule the last already written module.
     */
    private void parseSection(BufferedWriter writer,
                              YamlSection section,
                              YamlSection beforeModule) throws IOException {
        int keysSize = section.getKeys().size();
        int beforeKeysSize = beforeModule.getKeys().size();
        String currentKey; // The current key of the current module
        String currentBeforeKey; // The current key of the before module
        for (int i = 0; i < keysSize; i++) { // Go through each key of the module

            // Get current modules key and beforeModules key.
            // It may happen that the beforeModule has less keys, or no keys at all,
            // so deal with that:
            currentKey = section.getKeyAt(i);
            if (i < beforeKeysSize && !beforeModule.getKeys().isEmpty())
                currentBeforeKey = beforeModule.getKeyAt(i);
            else
                currentBeforeKey = "";

            // Only write this key, if its not equal to the currentBeforeKey
            // or... the other part is hard to explain.
            if (!currentKey.equals(currentBeforeKey) || (i != 0 && !section.getKeyAt(i - 1).equals(beforeModule.getKeyAt(i - 1)))) {

                StringBuilder spaces = new StringBuilder();
                // The current keys index/position in the list defines how much spaces are needed.
                spaces.append("  ".repeat(i));

                if (i == (keysSize - 1)) { // Only write top line breaks and comments to the last key in the list
                    for (int j = 0; j < section.getCountTopLineBreaks(); j++) {
                        writer.newLine();
                        writer.flush();
                    }

                    if (section.getComments() != null)
                        if (!section.getComments().isEmpty()) {
                            for (String comment :
                                    section.getComments()) {
                                // Adds support for Strings containing \n to split up comments
                                BufferedReader bufReader = new BufferedReader(new StringReader(comment));
                                String commentLine;
                                boolean isMultiline = false;
                                while ((commentLine = bufReader.readLine()) != null) {
                                    isMultiline = true;
                                    writer.write(spaces + "# " + commentLine);
                                    writer.newLine();
                                    writer.flush();
                                }

                                if (!isMultiline) {
                                    writer.write(spaces + "# " + comment);
                                    writer.newLine();
                                    writer.flush();
                                }
                            }
                        } else if (yaml.isWriteDefaultCommentsWhenEmptyEnabled) {
                            for (String comment :
                                    section.getComments()) {
                                // Adds support for Strings containing \n to split up comments
                                BufferedReader bufReader = new BufferedReader(new StringReader(comment));
                                String commentLine;
                                boolean isMultiline = false;
                                while ((commentLine = bufReader.readLine()) != null) {
                                    isMultiline = true;
                                    writer.write(spaces + "# " + commentLine);
                                    writer.newLine();
                                    writer.flush();
                                }

                                if (!isMultiline) {
                                    writer.write(spaces + "# " + comment);
                                    writer.newLine();
                                    writer.flush();
                                }
                            }
                        }
                }

                writer.write(spaces + currentKey + ": ");

                if (section.getValues() != null && i == (keysSize - 1)) { // Only write values to the last key in the list
                    if (!section.getValues().isEmpty() && !isOnlyNullsList(section.getValues())) { // Write values if they exist, else write defaults, else write nothing
                        if (section.getValues().size() == 1) { // Even if we only got one DYModule, it written as a list
                            SmartString value = section.getValue();
                            if (value != null) { // Only write if its not null
                                if (value.asString() != null) writer.write(value.asOutputString());
                            }
                            if (hasSideComment(section.getSideComments(), 0))
                                writer.write(" # " + section.getSideComment()); // Append side comment to value
                            writer.newLine();
                            writer.flush();
                        } else { // This means we got multiple values, aka a list
                            writer.newLine();
                            for (int j = 0; j < section.getValues().size(); j++) {
                                SmartString value = section.getValueAt(j);
                                if (value != null) {
                                    writer.write(spaces + "  - ");
                                    if (value.asString() != null)
                                        writer.write(value.asOutputString()); // Append the value
                                }
                                if (hasSideComment(section.getSideComments(), j))
                                    writer.write(" # " + section.getSideCommentAt(j)); // Append side comment to value
                                writer.newLine();
                                writer.flush();
                            }
                        }
                    } else if (yaml.isWriteDefaultValuesWhenEmptyEnabled) {
                        if (section.getDefValues() != null && !section.getDefValues().isEmpty()) {
                            if (section.getDefValues().size() == 1) {
                                SmartString defValue = section.getDefValue();
                                if (defValue != null) {
                                    if (defValue.asString() != null)
                                        writer.write(defValue.asOutputString());
                                }
                                if (hasSideComment(section.getDefSideComments(), 0))
                                    writer.write(" # " + section.getDefSideComment()); // Append side comment to value
                                writer.newLine();
                                writer.flush();
                            } else {
                                writer.newLine();
                                for (int j = 0; j < section.getDefValues().size(); j++) {
                                    SmartString value = section.getDefValueAt(j);
                                    if (value != null) {
                                        writer.write(spaces + "  - ");
                                        if (value.asString() != null)
                                            writer.write(value.asOutputString()); // Append the value
                                    }
                                    if (hasSideComment(section.getDefSideComments(), j))
                                        writer.write(" # " + section.getDefSideCommentAt(j)); // Append side comment to value
                                    writer.newLine();
                                    writer.flush();
                                }
                            }
                        } else {
                            writer.newLine();
                            writer.flush();
                        }
                    }

                } else {
                    writer.newLine();
                    writer.flush();
                }
            }
        }
    }

    private boolean hasSideComment(List<String> comments, int i) {
        try {
            if (comments.isEmpty()) return false;
            comments.get(i);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isOnlyNullsList(List<SmartString> values) {
        boolean hasValue = false;
        for (SmartString val :
                values) {
            if (val.asString() != null) {
                hasValue = true;
                break;
            }
        }
        return !hasValue;
    }
}
