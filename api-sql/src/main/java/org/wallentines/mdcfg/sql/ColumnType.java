package org.wallentines.mdcfg.sql;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ColumnType<T> {

    private final DataType<T> dataType;
    private final String parameter;

    public ColumnType(DataType<T> dataType, Object... parameter) {
        this.dataType = dataType;
        this.parameter = Arrays.stream(parameter).map(Object::toString).collect(Collectors.joining(","));
    }

    public ColumnType(DataType<T> dataType) {
        this.dataType = dataType;
        this.parameter = null;
    }

    public String getEncoded() {
        StringBuilder builder = new StringBuilder(dataType.getName());
        if(parameter != null) {
            builder.append('(').append(parameter).append(')');
        }
        return builder.toString();
    }

}
