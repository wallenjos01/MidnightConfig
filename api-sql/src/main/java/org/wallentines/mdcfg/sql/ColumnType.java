package org.wallentines.mdcfg.sql;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a column type in a table
 * @param <T> The type of data
 */
public class ColumnType<T> {

    private final DataType<T> dataType;
    private final String parameter;

    /**
     * Creates a new column types with the given data type and parameters
     * @param dataType The data type
     * @param parameter The parameters
     */
    public ColumnType(DataType<T> dataType, Object... parameter) {
        this.dataType = dataType;
        this.parameter = Arrays.stream(parameter).map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * Creates a new column types with the given data type
     * @param dataType The data type
     */
    public ColumnType(DataType<T> dataType) {
        this.dataType = dataType;
        this.parameter = null;
    }

    /**
     * Encodes the column type to a string
     * @return A string containing the data type name and a comma-separated list of parameters in parentheses.
     */
    public String getEncoded() {
        StringBuilder builder = new StringBuilder(dataType.getName());
        if(parameter != null) {
            builder.append('(').append(parameter).append(')');
        }
        return builder.toString();
    }

    /**
     * Gets the data type corresponding to this column type
     * @return The column's data type
     */
    public DataType<T> getDataType() {
        return dataType;
    }
}
