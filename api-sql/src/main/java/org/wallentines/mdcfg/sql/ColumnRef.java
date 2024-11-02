package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.StatementBuilder;
import org.wallentines.mdcfg.sql.stmt.Term;

public class ColumnRef implements Term {

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
    public void write(StatementBuilder builder) {
        builder.append(table).append(".").append(column);
    }

    @Override
    public String toString() {
        return encode();
    }
}
