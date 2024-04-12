package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

import java.util.*;
import java.util.stream.Collectors;

public class TableSchema {

    private final List<Column> columns = new ArrayList<>();
    private final List<String> columnNames = new ArrayList<>();
    private final HashMap<String, Integer> indicesByName = new HashMap<>();


    private TableSchema(List<Tuples.T2<String, Column>> columns) {

        for(Tuples.T2<String, Column> t : columns) {
            int index = this.columns.size();
            this.columns.add(t.p2);
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
        return columns.get(indicesByName.get(column)).type;
    }

    public ColumnType<?> getType(int column) {
        return columns.get(column).type;
    }

    public EnumSet<ColumnFlag> getFlags(String column) {
        return columns.get(indicesByName.get(column)).flags;
    }

    public EnumSet<ColumnFlag> getFlags(int column) {
        return columns.get(column).flags;
    }

    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    public String encodeColumn(int column) {
        return columns.get(column).encode();
    }

    public String encodeColumn(String column) {
        return columns.get(indicesByName.get(column)).encode();
    }


    public TableSchema toUpperCase() {
        Builder out = builder();
        for(int i = 0; i < columns.size() ; i++) {
            out.addColumn(columnNames.get(i).toUpperCase(), columns.get(i));
        }
        return out.build();
    }

    public Builder asBuilder() {
        Builder out = builder();
        for(int i = 0; i < columns.size() ; i++) {
            out.addColumn(columnNames.get(i), columns.get(i));
        }
        return out;
    }

    public TableSchema subSchema(String... keys) {
        Builder out = builder();
        for(String key : keys) {
            out.addColumn(key, columns.get(indicesByName.get(key)));
        }
        return out.build();
    }

    public TableSchema subSchema(List<String> keys) {
        Builder out = builder();
        for(String key : keys) {
            out.addColumn(key, columns.get(indicesByName.get(key)));
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
        PRIMARY_KEY("PRIMARY KEY"),
        NOT_NULL("NOT NULL"),
        AUTO_INCREMENT("AUTO INCREMENT"),
        UNIQUE("UNIQUE");

        final String name;

        ColumnFlag(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
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

        public String encode() {
            return type.getEncoded() + " " + flags.stream().map(ColumnFlag::getName).collect(Collectors.joining(" "));
        }
    }
}
