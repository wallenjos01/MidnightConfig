package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class Insert extends DMLStatement {

    private final PreparedStatement statement;
    private final TableSchema schema;

    public Insert(SQLConnection connection, String table, TableSchema schema) {
        super(connection);

        this.schema = schema;
        this.statement = prepare(table, schema);
    }

    protected PreparedStatement prepare(String table, TableSchema schema) {
        try {
            StatementBuilder builder = new StatementBuilder("INSERT INTO ").append(table);
            if(schema.getColumnCount() > 0) {
                builder.append("(");
                boolean first = true;
                for(Column c : schema.getColumns()) {
                    if(!first) {
                        builder.append(", ");
                    }
                    if(!c.hasConstraint(Constraint.Type.AUTO_INCREMENT)) {
                        builder.append(connection.quoteIdentifier(c.getName()));
                    }
                    first = false;
                }
                builder.append(")");
            }

            builder.append(" VALUES (");
            for(int i = 0; i < schema.getColumnCount() ; i++) {
                if(i > 0) builder.append(",");
                builder.appendUnknown();
            }
            builder.append(")");

            return builder.prepare(connection, true);
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
            for(Column c : schema.getColumns()) {

                ConfigObject obj = values.get(c.getName());
                DataValue.writeSerialized(ConfigContext.INSTANCE, obj, statement, index++, c.getType().getDataType());
            }
            statement.addBatch();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to add row to INSERT statement!", ex);
        }
        return this;
    }

    @Override
    public UpdateResult execute() {
        try {
            int[] out = statement.executeBatch();
            ResultSet keys = statement.getGeneratedKeys();
            QueryResult generatedKeys = null;
            if(keys != null) {
                generatedKeys = QueryResult.fromResultSet(keys, connection);
            }

            statement.close();
            return new UpdateResult(out, generatedKeys);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute INSERT statement!", ex);
        }
    }
}
