package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Insert extends DMLStatement {

    private final PreparedStatement statement;
    private final List<String> columns;

    public Insert(SQLConnection connection, String table, TableSchema columns) {
        super(connection);
        List<String> names = new ArrayList<>();
        for(Column col : columns.getColumns()) {
            if(col.hasConstraint(Constraint.Type.AUTO_INCREMENT)) continue;
            names.add(col.getName());
        }

        this.columns = List.copyOf(names);
        this.statement = prepare(table, names);
    }


    public Insert(SQLConnection connection, String table, Collection<String> columns) {
        super(connection);
        this.columns = List.copyOf(columns);
        this.statement = prepare(table, columns);
    }

    public Insert(SQLConnection connection, String table, ConfigSection values) {
        super(connection);
        this.columns = List.copyOf(values.getKeys());
        this.statement = prepare(table, columns);
        addRow(values);
    }


    protected PreparedStatement prepare(String table, Collection<String> columns) {
        try {
            StringBuilder builder = new StringBuilder("INSERT INTO ")
                    .append(table);
            if(!columns.isEmpty()) builder.append("(").append(String.join(",", columns)).append(")");

            builder.append(" VALUES (");
            for(int i = 0 ; i < columns.size() ; i++) {
                if(i > 0) builder.append(",");
                builder.append("?");
            }
            builder.append(");");

            return connection.getInternal().prepareStatement(builder.toString());
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to prepare INSERT statement!", ex);
        }
    }


    public Insert addRow(Collection<DataValue<?>> values) {

        try {
            int index = 1;
            for(DataValue<?> value : values) {
                value.write(statement, index++);
            }
            statement.addBatch();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to add row to INSERT statement!", ex);
        }
        return this;
    }

    public Insert addRow(ConfigSection values) {

        try {
            int index = 1;
            for(String s : columns) {
                DataValue.writeSerialized(ConfigContext.INSTANCE, values.get(s), statement, index++);
            }
            statement.addBatch();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to add row to INSERT statement!", ex);
        }
        return this;
    }

    @Override
    public int[] execute() {
        try {
            int[] out = statement.executeBatch();
            statement.close();
            return out;
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute INSERT statement!", ex);
        }
    }
}
