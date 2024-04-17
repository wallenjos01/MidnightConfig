package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.SQLUtil;
import org.wallentines.mdcfg.sql.TableSchema;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        StatementBuilder stmt = new StatementBuilder().append("CREATE TABLE ");

        if(ifNotExists) {
            stmt.append("IF NOT EXISTS ");
        }

        stmt.append(table + "(")
                .append(connection.getType().getDialect().writeTableSchema(connection, schema))
                .append(");");

        try(PreparedStatement prep = stmt.prepare(connection)) {
            return prep.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute CREATE TABLE statement!", ex);
        }
    }
}
