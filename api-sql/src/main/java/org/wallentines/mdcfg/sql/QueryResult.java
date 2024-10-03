package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represents the result of a SQL query operation.
 */
public class QueryResult {

    private final SQLConnection connection;
    private final List<Row> rows;

    /**
     * Creates an empty query result corresponding to the given connection.
     * @param connection The database connection to which this query result corresponds.
     */
    public QueryResult(SQLConnection connection) {
        this.connection = connection;
        this.rows = new ArrayList<>();
    }

    /**
     * Gets the number of rows in the query result.
     * @return The number of rows.
     */
    public int rows() {
        return rows.size();
    }

    /**
     * Gets the row at the given index.
     * @param index The row index.
     * @return A queries row.
     */
    public Row get(int index) {
        return rows.get(index);
    }

    /**
     * Adds a row to the result
     * @param values A list of column names and their values.
     */
    public void addRow(List<Tuples.T2<String, DataValue<?>>> values) {
        this.rows.add(new Row(values));
    }

    /**
     * Represents a row in the result.
     */
    public class Row {

        private final List<Tuples.T2<String, DataValue<?>>> values;
        private final HashMap<String, Integer> rowsByName = new HashMap<>();

        /**
         * Creates a new row with the given values.
         * @param values The values in the row
         */
        public Row(List<Tuples.T2<String, DataValue<?>>> values) {
            this.values = values;

            int index = 0;
            for(Tuples.T2<String, DataValue<?>> t2 : values) {
                rowsByName.put(t2.p1, index++);
            }
        }

        /**
         * Gets the number of columns in the row.
         * @return The number of columns in the row.
         */
        public int columns() {
            return values.size();
        }

        /**
         * Determines if a column with the given name exists in the result.
         * @param key The column name.
         * @return Whether a columns with the given name is in the result.
         */
        public boolean hasColumn(String key) {
            return rowsByName.containsKey(connection.fixIdentifier(key));
        }

        /**
         * Gets the object in the column with the given name
         * @param column The column name
         * @return The value in the column
         * @throws NoSuchElementException If there is no element in the column
         */
        public DataValue<?> get(String column) {

            String id = connection.fixIdentifier(column);
            Tuples.T2<String, DataValue<?>> val = values.get(rowsByName.get(id));
            if(val == null) throw new NoSuchElementException("Unable to find element for column " + column + "!");

            return val.p2;
        }

        /**
         * Gets the object in the column with the given index
         * @param index The column index
         * @return The value in the column
         * @throws NoSuchElementException If there is no element in the column
         */
        public DataValue<?> get(int index) {

            Tuples.T2<String, DataValue<?>> val = values.get(index);
            if(val == null) throw new NoSuchElementException("Unable to find element for column " + index + "!");

            return val.p2;
        }

        /**
         * Gets the object in the column with the given name
         * @param column The column name
         * @param type The type of the object
         * @return The object in the column
         * @throws IllegalArgumentException If the given type does not match the type of the object
         */
        @SuppressWarnings("unchecked")
        public <T> DataValue<T> get(String column, DataType<T> type) {

            DataValue<?> val = get(column);
            if(val.getType() != type) {
                throw new IllegalArgumentException("Value was not of the required type!");
            }

            return ((DataValue<T>) val);
        }

        /**
         * Gets the object in the column with the given index
         * @param index The column index
         * @param type The type of the object
         * @return The object in the column
         * @throws IllegalArgumentException If the given type does not match the type of the object
         */
        @SuppressWarnings("unchecked")
        public <T> DataValue<T> get(int index, DataType<T> type) {

            DataValue<?> val = get(index);
            if(val.getType() != type) {
                throw new IllegalArgumentException("Value was not of the required type!");
            }

            return ((DataValue<T>) val);
        }

        /**
         * Gets the object in the column with the given name
         * @param column The column name
         * @return The object in the column
         */
        public Object getValue(String column) {

            return get(column).getValue();
        }

        /**
         * Gets the value in the column with the given index
         * @param index The column index
         * @param type The type of the object
         * @return The object in the column
         * @throws IllegalArgumentException If the given type does not match the type of the object
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue(int index, DataType<T> type) {

            DataValue<?> val = get(index);
            if(val.getType() != type) {
                throw new IllegalArgumentException("Value was not of the required type!");
            }

            return ((DataValue<T>) val).getValue();
        }

        /**
         * Converts the result to a ConfigSection
         * @return A ConfigSection representing the result
         */
        public ConfigSection toConfigSection() {
            ConfigSection out = new ConfigSection();
            for(Tuples.T2<String, DataValue<?>> t : values) {
                t.p2.setConfig(out, t.p1);
            }
            return out;
        }

    }

    public static QueryResult fromResultSet(ResultSet resultSet, SQLConnection connection) throws SQLException {

        int cols = resultSet.getMetaData().getColumnCount();
        QueryResult res = new QueryResult(connection);
        while(resultSet.next()) {
            List<Tuples.T2<String, DataValue<?>>> values = new ArrayList<>();
            for(int i = 1 ; i <= cols ; i++) {

                int type = resultSet.getMetaData().getColumnType(i);
                if(type == Types.NULL) {
                    continue;
                }

                DataType<?> dt = DataType.get(type);
                if(dt == null) {
                    throw new IllegalStateException("Unknown column type " + type + "!");
                }

                values.add(new Tuples.T2<>(resultSet.getMetaData().getColumnName(i), dt.read(resultSet, i)));
            }
            res.addRow(values);
        }

        return res;
    }

}
