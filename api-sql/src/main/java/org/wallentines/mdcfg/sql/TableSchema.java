package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

import java.util.*;

public class TableSchema {

    private final List<ColumnType> values = new ArrayList<>();
    private final List<String> columnNames = new ArrayList<>();
    private final HashMap<String, Integer> indicesByName = new HashMap<>();


    private TableSchema(List<Tuples.T2<String, ColumnType>> columns) {

        for(Tuples.T2<String, ColumnType> t : columns) {
            int index = values.size();
            values.add(t.p2);
            columnNames.add(t.p1);
            indicesByName.put(t.p1, index);
        }

    }

    public Collection<String> getColumnNames() {
        return columnNames;
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public ColumnType getType(String column) {
        return values.get(indicesByName.get(column));
    }

    public ColumnType getType(int column) {
        return values.get(column);
    }
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    public TableSchema toUpperCase() {
        Builder out = builder();
        for(int i = 0 ; i < values.size() ; i++) {
            out.withColumn(columnNames.get(i), values.get(i));
        }
        return out.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        List<Tuples.T2<String, ColumnType>> values = new ArrayList<>();
        public Builder() { }

        public Builder withColumn(String key, ColumnType value) {

            if(!SQLUtil.VALID_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid column name " + key + "!");
            }

            values.add(new Tuples.T2<>(key, value));
            return this;
        }

        public TableSchema build() {
            return new TableSchema(values);
        }
    }


}
