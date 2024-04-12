package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.SQLUtil;
import org.wallentines.mdcfg.sql.TableSchema;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class CreateTable extends DDLStatement {

    public final String table;
    public final TableSchema schema;
    public boolean ifNotExists;

    public CreateTable(SQLConnection connection, String table, TableSchema schema) {
        super(connection);
        SQLUtil.validate(table);
        this.table = table;
        this.schema = schema;
    }

    public CreateTable ifNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
        return this;
    }

    @Override
    public boolean execute() {

        StringBuilder stmt = new StringBuilder("CREATE TABLE ")
                .append(table);

        if(ifNotExists) {
            stmt.append(" IF NOT EXISTS");
        }

        stmt.append(" (")
                .append(schema.getColumnNames().stream()
                        .map(key -> key + " " + schema.encodeColumn(key))
                        .collect(Collectors.joining(", ")))
                .append(");");

        try(PreparedStatement prep = connection.getInternal().prepareStatement(stmt.toString())) {
            return prep.execute();
        } catch (SQLException ex) {

            throw new IllegalStateException("Unable to execute CREATE TABLE statement!", ex);
        }
    }
}
