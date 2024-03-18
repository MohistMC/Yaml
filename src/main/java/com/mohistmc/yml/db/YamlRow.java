package com.mohistmc.yml.db;

import com.mohistmc.yml.SmartString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the row of a {@link YamlTable}. Example:
 * <pre>
 * column1 | column2
 * =================
 * rock    | paper   <--- This is the row at index 0
 * tree    | grass   <--- This is the row at index 1
 * </pre>
 *
 * @see YamlColumn
 */
public record YamlRow(int rowIndex, Map<SmartString, YamlColumn> valuesAndColumns) {

    /**
     * Values are read from left to right. Example table:
     * <pre>
     *     column1 | column2
     *     =================
     *     rock    | paper
     * </pre>
     * The examples, returned list has two {@link SmartString}s. <br>
     * The first one contains 'rock' and the second one 'paper'. <br>
     */
    public List<SmartString> getValues() {
        return new ArrayList<>(valuesAndColumns.keySet());
    }

    public YamlColumn getColumnFromValue(SmartString value) {
        return valuesAndColumns.get(value);
    }

    /**
     * Returns the linked {@link SmartString} for the provided {@link YamlColumn}.
     *
     * @throws NullPointerException if the provided column is null, or couldn't be found in the map.
     */
    public SmartString getValueFromColumn(YamlColumn column) {
        Objects.requireNonNull(column);
        SmartString[] values = valuesAndColumns.keySet().toArray(new SmartString[0]);
        int index = 0;
        SmartString val = null;
        for (YamlColumn col :
                valuesAndColumns.values()) {
            if (col.getName().equals(column.getName()))
                val = values[index];
            index++;
        }
        if (val == null)
            throw new NullPointerException("Column '" + column.getName() + "' couldn't be found in: " + Arrays.toString(values));
        else
            return val;
    }

}
