package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            serialized.add(new Tuples.T2<>(key, section.get(key)));
        }
        return this;
    }

    public Update where(Condition where) {
        this.where = where;
        return this;
    }

    @Override
    public int[] execute() {

        StringBuilder query = new StringBuilder("UPDATE ")
                .append(table).append(" SET ")
                .append(values.stream().map(c -> c.p1 + " = ?").collect(Collectors.joining(", ")))
                .append(serialized.stream().map(c -> c.p1 + " = ?").collect(Collectors.joining(", ")));

        if (where != null) {
            query.append(" WHERE ").append(where.encode());
        }
        query.append(";");

        try(PreparedStatement pst = connection.getInternal().prepareStatement(query.toString())) {

            int index = 1;
            for(Tuples.T2<String, DataValue<?>> s : values) {
                s.p2.write(pst, index++);
            }
            for(Tuples.T2<String, ConfigObject> s : serialized) {
                DataValue.writeSerialized(ConfigContext.INSTANCE, s.p2, pst, index++);
            }

            if(where != null) {
                where.writeArguments(pst, index);
            }

            return new int[] { pst.executeUpdate() };

        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to add row to UPDATE statement!", ex);
        }
    }
}
