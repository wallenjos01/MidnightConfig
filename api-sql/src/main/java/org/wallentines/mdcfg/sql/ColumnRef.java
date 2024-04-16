package org.wallentines.mdcfg.sql;

public class ColumnRef {

    public final String table;
    public final String column;
    public boolean applyTablePrefix = true;

    public ColumnRef(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public ColumnRef withPrefix(String prefix) {
        return new ColumnRef(prefix + table, column);
    }


    public ColumnRef skipTablePrefix() {
        this.applyTablePrefix = false;
        return this;
    }

    public String encode() {
        return table + "(" + column + ")";
    }


    @Override
    public String toString() {
        return encode();
    }
}
