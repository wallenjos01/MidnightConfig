package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class QueryResult {

    private final SQLConnection connection;
    private final List<Row> rows;

    public QueryResult(SQLConnection connection) {
        this.connection = connection;
        this.rows = new ArrayList<>();
    }

    public int rows() {
        return rows.size();
    }

    public Row get(int index) {
        return rows.get(index);
    }

    public void addRow(List<Tuples.T2<String, DataValue<?>>> values) {
        this.rows.add(new Row(values));
    }

    public class Row {

        private final List<Tuples.T2<String, DataValue<?>>> values;
        private final HashMap<String, Integer> rowsByName = new HashMap<>();

        public Row(List<Tuples.T2<String, DataValue<?>>> values) {
            this.values = values;

            int index = 0;
            for(Tuples.T2<String, DataValue<?>> t2 : values) {
                rowsByName.put(t2.p1, index++);
            }
        }

        public int columns() {
            return values.size();
        }

        public boolean hasColumn(String key) {
            return rowsByName.containsKey(connection.fixIdentifier(key));
        }

        public DataValue<?> get(String column) {

            String id = connection.fixIdentifier(column);
            Tuples.T2<String, DataValue<?>> val = values.get(rowsByName.get(id));
            if(val == null) throw new NoSuchElementException("Unable to find element for column " + column + "!");

            return val.p2;
        }

        public Object getValue(String column) {

            return get(column).getValue();
        }

        public ConfigSection toConfigSection() {
            ConfigSection out = new ConfigSection();
            for(Tuples.T2<String, DataValue<?>> t : values) {
                genericSet(out, t.p1, t.p2);
            }
            return out;
        }

    }

    private static <T> void genericSet(ConfigSection sec, String columnName, DataValue<T> value) {
        sec.set(columnName, value.getValue(), value.getType().getSerializer());
    }

}
