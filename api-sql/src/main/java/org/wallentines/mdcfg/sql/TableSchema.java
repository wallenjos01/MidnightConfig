package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TableSchema {

    private final List<Column> columns;
    private final HashMap<String, Integer> indicesByName = new HashMap<>();
    private final List<TableConstraint<?>> constraints;

    private TableSchema(List<Column> columns, List<TableConstraint<?>> constraints) {

        this.columns = List.copyOf(columns);
        this.constraints = List.copyOf(constraints);

        int index = 0;
        for(Column c : columns) {
            indicesByName.put(c.getName(), index++);
        }
    }

    /**
     * Gets a list of all the columns in the schema
     * @return A list of columns in the schema
     */
    public Collection<Column> getColumns() {
        return columns;
    }

    /**
     * Gets the names of the columns in the table
     * @return The column names
     */
    public Collection<String> getColumnNames() {
        return indicesByName.keySet();
    }

    /**
     * Gets the number of columns in the table
     * @return The number of columns
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Gets a reference to the column at the given index
     * @param index The index to lookup
     * @return A reference to the column at that index
     * @throws IndexOutOfBoundsException if there is no column at that index
     */
    public Column getColumn(int index) {
        return columns.get(index);
    }

    /**
     * Gets a reference to the column with the given name
     * @param name index to lookup
     * @return A reference to the column with that name
     */
    public Column getColumn(String name) {
        return columns.get(indicesByName.get(name));
    }

    /**
     * Gets the table constraints on this table
     * @return The table constraints
     */
    public Collection<TableConstraint<?>> getConstraints() {
        return constraints;
    }

    /**
     * Creates a ConfigSection from the given row, using this schema as a template
     * @param row The row from a query result
     * @return A new config section created from that row
     */
    public ConfigSection createSection(QueryResult.Row row) {

        ConfigSection out = new ConfigSection();
        for(Column c : columns) {
            String name = c.getName();
            if(row.hasColumn(name)) {
                row.get(name).setConfig(out, name);
            }
        }

        return out;
    }


    /**
     * Creates a new table schema builder with the same columns
     * @return A new builder
     */
    public Builder asBuilder() {
        Builder out = builder();
        for (Column column : columns) {
            out.withColumn(column);
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
            out.withColumn(columns.get(indicesByName.get(key)));
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
            out.withColumn(columns.get(indicesByName.get(key)));
        }
        return out.build();
    }

    /**
     * Creates a new table schema using the keys and types the given ConfigSection
     * @param section The ConfigSection to read
     * @return A new TableSchema
     */
    public static TableSchema fromSection(ConfigSection section) {
        Builder builder = builder();
        for(String key : section.getKeys()) {
            DataType<?> type = DataType.fromSerialized(section.get(key));
            if(type != null) builder.withColumn(key, type);
        }
        return builder.build();
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
        private final List<Column> values = new ArrayList<>();
        private final List<TableConstraint<?>> constraints = new ArrayList<>();

        /**
         * Adds a column to the table schema
         * @param name The column name
         * @param type The column type
         * @return A reference to self
         */
        public Builder withColumn(String name, DataType<?> type) {
            values.add(new Column.Builder(name, new ColumnType<>(type)).build());
            return this;
        }

        /**
         * Adds a column to the table schema
         * @param name The column name
         * @param type The column type
         * @return A reference to self
         */
        public Builder withColumn(String name, ColumnType<?> type) {
            values.add(new Column.Builder(name, type).build());
            return this;
        }

        /**
         * Adds a column to the table schema
         * @param column The column
         * @return A reference to self
         */
        public Builder withColumn(Column.Builder column) {

            values.add(column.build());
            return this;
        }

        /**
         * Adds a column to the table schema
         * @param column The column
         * @return A reference to self
         */
        public Builder withColumn(Column column) {

            values.add(column);
            return this;
        }

        /**
         * Adds a table constraint to the table schema
         * @param constraint The table constraint
         * @return A reference to self
         */
        public Builder withTableConstraint(TableConstraint<?> constraint) {
            constraints.add(constraint);
            return this;
        }

        /**
         * Constructs a table schema from this builder
         * @return A new table schema
         */
        public TableSchema build() {
            assert !values.isEmpty();
            return new TableSchema(values, constraints);
        }
    }
}
