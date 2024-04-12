package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

import java.util.*;

public class TableSchema {

    private final List<Column> values = new ArrayList<>();
    private final List<String> columnNames = new ArrayList<>();
    private final HashMap<String, Integer> indicesByName = new HashMap<>();


    private TableSchema(List<Tuples.T2<String, Column>> columns) {

        for(Tuples.T2<String, Column> t : columns) {
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

    public ColumnType<?> getType(String column) {
        return values.get(indicesByName.get(column)).type;
    }

    public ColumnType<?> getType(int column) {
        return values.get(column).type;
    }

    public EnumSet<ColumnFlag> getFlags(String column) {
        return values.get(indicesByName.get(column)).flags;
    }

    public EnumSet<ColumnFlag> getFlags(int column) {
        return values.get(column).flags;
    }

    public String getColumnName(int column) {
        return columnNames.get(column);
    }


    public TableSchema toUpperCase() {
        Builder out = builder();
        for(int i = 0 ; i < values.size() ; i++) {
            out.addColumn(columnNames.get(i).toUpperCase(), values.get(i));
        }
        return out.build();
    }

    public Builder asBuilder() {
        Builder out = builder();
        for(int i = 0 ; i < values.size() ; i++) {
            out.addColumn(columnNames.get(i), values.get(i));
        }
        return out;
    }

    public TableSchema subSchema(String... keys) {
        Builder out = builder();
        for(String key : keys) {
            out.addColumn(key, values.get(indicesByName.get(key)));
        }
        return out.build();
    }

    public TableSchema subSchema(List<String> keys) {
        Builder out = builder();
        for(String key : keys) {
            out.addColumn(key, values.get(indicesByName.get(key)));
        }
        return out.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        List<Tuples.T2<String, Column>> values = new ArrayList<>();

        public Builder withColumn(String key, DataType<?> type, ColumnFlag... flags) {

            return withColumn(key, new ColumnType<>(type), flags);
        }

        public Builder withColumn(String key, ColumnType<?> type, ColumnFlag... flags) {

            EnumSet<ColumnFlag> outFlags;
            if(flags.length == 0) {
                outFlags = EnumSet.noneOf(ColumnFlag.class);
            } else {
                outFlags = EnumSet.copyOf(Arrays.asList(flags));
            }
            return withColumn(key, type, outFlags);
        }


        public Builder withColumn(String key, ColumnType<?> type, EnumSet<ColumnFlag> flags) {

            if(!SQLUtil.VALID_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid column name " + key + "!");
            }

            values.add(new Tuples.T2<>(key, new Column(type, flags)));
            return this;
        }


        private void addColumn(String key, Column column) {
            values.add(new Tuples.T2<>(key, column));
        }

        public TableSchema build() {
            return new TableSchema(values);
        }
    }

    public enum ColumnFlag {
        PRIMARY_KEY,
        NOT_NULL,
        AUTO_INCREMENT,
        UNIQUE
    }

    private static class Column {

        private final ColumnType<?> type;
        private final EnumSet<ColumnFlag> flags;

        public Column(ColumnType<?> type, EnumSet<ColumnFlag> flags) {
            this.type = type;
            this.flags = flags;
        }

        public Column(ColumnType<?> type) {
            this.type = type;
            this.flags = EnumSet.noneOf(ColumnFlag.class);
        }
    }
}
