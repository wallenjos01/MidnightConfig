package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.Condition;
import org.wallentines.mdcfg.sql.DataType;
import org.wallentines.mdcfg.sql.DataValue;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatementBuilder {

    private final List<Entry> entries = new ArrayList<>();

    public StatementBuilder() {
    }

    public StatementBuilder(String initial) {
        append(initial);
    }

    public PreparedStatement prepare(SQLConnection conn) throws SQLException {

        StringBuilder out = new StringBuilder();
        for(Entry ent : entries) {
            out.append(ent.write());
        }
        out.append(";");

        PreparedStatement stmt = conn.getInternal().prepareStatement(out.toString());

        int index = 0;
        for(Entry ent : entries) {
            if(ent.isParam()) index++;
            if(ent.value != null) {
                ent.value.write(stmt, index);
            }
        }

        return stmt;
    }

    public StatementBuilder append(String text) {
        Entry ent = new Entry();
        ent.text = text;
        entries.add(ent);
        return this;
    }

    public StatementBuilder appendValue(DataValue<?> value) {
        Entry ent = new Entry();
        ent.value = value;
        entries.add(ent);
        return this;
    }

    public StatementBuilder appendList(List<String> strings) {
        return append(String.join(", ", strings));
    }

    public StatementBuilder appendUnknown() {
        Entry ent = new Entry();
        ent.isUnknown = true;
        entries.add(ent);
        return this;
    }

    public StatementBuilder append(StatementBuilder other) {
        this.entries.addAll(other.entries);
        return this;
    }

    public StatementBuilder appendCondition(Condition condition) {
        condition.encode(this);
        return this;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private static class Entry {

        String text;
        boolean isUnknown;
        DataValue<?> value;

        boolean isParam() {
            return isUnknown || value != null;
        }

        String write() {
            if(isParam()) {
                return "?";
            }
            return text;
        }

    }

}
