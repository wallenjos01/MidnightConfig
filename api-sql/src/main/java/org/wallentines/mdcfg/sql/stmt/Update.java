package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.Condition;
import org.wallentines.mdcfg.sql.DataValue;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.SQLUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Update extends DMLStatement {

    private final String table;
    private final List<Tuples.T2<String, DataValue<?>>> values;
    private final List<Tuples.T2<String, ConfigObject>> serialized;
    private Condition where;

    public Update(SQLConnection connection, String table) {
        super(connection);

        SQLUtil.validate(table);

        this.table = table;
        this.values = new ArrayList<>();
        this.serialized = new ArrayList<>();

    }

    public Update withValue(String column, DataValue<?> value) {
        values.add(new Tuples.T2<>(column, value));
        return this;
    }

    public Update withRow(ConfigSection section) {
        for(String key : section.getKeys()) {
            if(section.has(key)) serialized.add(new Tuples.T2<>(key, section.get(key)));
        }
        return this;
    }

    public Update where(Condition where) {
        this.where = where;
        return this;
    }

    @Override
    public int[] execute() {

        StatementBuilder query = new StatementBuilder("UPDATE ")
                .append(table).append(" SET ");

        int index = 0;
        for(Tuples.T2<String, DataValue<?>> ent : values) {
            if(index++ > 0) {
                query.append(",");
            }
            query.append(ent.p1 + " = ").appendValue(ent.p2);
        }
        for(Tuples.T2<String, ConfigObject> ent : serialized) {
            if (index++ > 0) {
                query.append(",");
            }
            query.append(ent.p1 + " = ").appendUnknown();
        }

        if (where != null) {
            query.append(" WHERE ").appendCondition(where);
        }

        try(PreparedStatement pst = query.prepare(connection)) {

            index = values.size() + 1;
            for(Tuples.T2<String, ConfigObject> s : serialized) {
                DataValue.writeSerialized(ConfigContext.INSTANCE, s.p2, pst, index++);
            }

            return new int[] { pst.executeUpdate() };

        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to add row to UPDATE statement!", ex);
        }
    }
}
