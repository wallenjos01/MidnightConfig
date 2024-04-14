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

    /**
     * Gets the names of the columns in the table
     * @return The column names
     */
    public Collection<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Gets the number of columns in the table
     * @return The number of columns
     */
    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * Gets the type of the column with the given name
     * @param column The column name to lookup
     * @return The column's data type
     */
    public ColumnType<?> getType(String column) {
        return columns.get(indicesByName.get(column)).type;
    }

    /**
     * Gets the type of the column at the given index
     * @param column The column index
     * @return The column's data type
     */
    public ColumnType<?> getType(int column) {
        return columns.get(column).type;
    }

    /**
     * Gets the constraints of the column with the given name
     * @param column The column name to lookup
     * @return The column's constraints
     */
    public EnumSet<Constraint> getConstraints(String column) {
        return columns.get(indicesByName.get(column)).flags;
    }

    /**
     * Gets the constraints of the column at the given index
     * @param column The column index
     * @return The column's constraints
     */
    public EnumSet<Constraint> getConstraints(int column) {
        return columns.get(column).flags;
    }

    /**
     * Gets the name of the column at the given index
     * @param column The column index
     * @return The column's name
     */
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    /**
     * Encodes the type and constraints of the column at the given index, for use in a CREATE COLUMN statement
     * @param column The column index
     * @return The column's encoded type
     */
    public String encodeColumn(int column) {
        return columns.get(column).encode();
    }

    /**
     * Encodes the type and constraints of the column with the given name, for use in a CREATE COLUMN statement
     * @param column The column's name
     * @return The column's encoded type
     */
    public String encodeColumn(String column) {
        return columns.get(indicesByName.get(column)).encode();
    }

    /**
     * Creates a new table schema builder with the same columns
     * @return A new builder
     */
    public Builder asBuilder() {
        Builder out = builder();
        for(int i = 0; i < columns.size() ; i++) {
            out.addColumn(columnNames.get(i), columns.get(i));
        }
        return out;
    }

    /**
     * Creates a new table schema builder with only the given columns
     * @return A new builder
     */
    public TableSchema subSchema(String... keys) {
        Builder out = builder();
        for(String key : keys) {
            out.addColumn(key, columns.get(indicesByName.get(key)));
        }
        return out.build();
    }

    /**
     * Creates a new table schema builder with only the given columns
     * @return A new builder
     */
    public TableSchema subSchema(List<String> keys) {
        Builder out = builder();
        for(String key : keys) {
            out.addColumn(key, columns.get(indicesByName.get(key)));
        }
        return out.build();
    }

    /**
     * Creates a new column builder
     * @return An empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Used for creating table schemas
     */
    public static class Builder {
        private final List<Tuples.T2<String, Column>> values = new ArrayList<>();

        /**
         * Adds a column to the table schema
         * @param key The column's name
         * @param type The column's type
         * @param constraints The column's constraints
         * @return A reference to self
         */
        public Builder withColumn(String key, DataType<?> type, Constraint... constraints) {

            return withColumn(key, new ColumnType<>(type), constraints);
        }

        /**
         * Adds a column to the table schema
         * @param key The column's name
         * @param type The column's type
         * @param constraints The column's constraints
         * @return A reference to self
         */
        public Builder withColumn(String key, ColumnType<?> type, Constraint... constraints) {

            EnumSet<Constraint> outFlags;
            if(constraints.length == 0) {
                outFlags = EnumSet.noneOf(Constraint.class);
            } else {
                outFlags = EnumSet.copyOf(Arrays.asList(constraints));
            }
            return withColumn(key, type, outFlags);
        }

        /**
         * Adds a column to the table schema
         * @param key The column's name
         * @param type The column's type
         * @param constraints The column's constraints
         * @return A reference to self
         */
        public Builder withColumn(String key, ColumnType<?> type, EnumSet<Constraint> constraints) {

            if(!SQLUtil.VALID_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid column name " + key + "!");
            }

            values.add(new Tuples.T2<>(key, new Column(type, constraints)));
            return this;
        }

        private void addColumn(String key, Column column) {
            values.add(new Tuples.T2<>(key, column));
        }

        /**
         * Constructs a table schema from this builder
         * @return A new table schema
         */
        public TableSchema build() {
            return new TableSchema(values);
        }
    }

    /**
     * Represents a column constraint
     */
    public enum Constraint {
        PRIMARY_KEY("PRIMARY KEY"),
        NOT_NULL("NOT NULL"),
        AUTO_INCREMENT("AUTO_INCREMENT"),
        UNIQUE("UNIQUE");

        final String name;

        Constraint(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static class Column {

        private final ColumnType<?> type;
        private final EnumSet<Constraint> flags;

        public Column(ColumnType<?> type, EnumSet<Constraint> flags) {
            this.type = type;
            this.flags = flags;
        }

        public Column(ColumnType<?> type) {
            this.type = type;
            this.flags = EnumSet.noneOf(Constraint.class);
        }

        public String encode() {
            return type.getEncoded() + " " + flags.stream().map(Constraint::getName).collect(Collectors.joining(" "));
        }
    }
}
