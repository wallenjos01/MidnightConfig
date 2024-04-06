package org.wallentines.mdcfg.sql;

import java.util.*;

public class TableSchema {

    private final Map<String, ColumnType> columns;

    private TableSchema(Map<String, ColumnType> columns) {
        this.columns = Map.copyOf(columns);
    }

    public Collection<String> getColumnNames() {
        return columns.keySet();
    }

    public ColumnType getType(String column) {
        return columns.get(column);
    }

    public TableSchema toUpperCase() {
        Builder out = builder();
        for(String key : columns.keySet()) out.withColumn(key.toUpperCase(), columns.get(key));
        return out.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Map<String, ColumnType> values = new HashMap<>();
        public Builder() { }

        public Builder withColumn(String key, ColumnType value) {

            if(!SQLUtil.VALID_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid column name " + key + "!");
            }

            values.put(key, value);
            return this;
        }

        public TableSchema build() {
            return new TableSchema(values);
        }
    }


}
