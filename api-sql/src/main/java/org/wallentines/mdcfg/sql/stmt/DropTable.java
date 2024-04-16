package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DropTable extends DDLStatement {

    public final String table;
    public boolean ifExists;

    public DropTable(SQLConnection connection, String table) {
        super(connection);
        this.table = table;
    }

    public DropTable ifExists() {
        this.ifExists = true;
        return this;
    }

    @Override
    public boolean execute() {

        StatementBuilder stmt = new StatementBuilder().append("DROP TABLE ");

        if(ifExists) {
            stmt.append("IF EXISTS ");
        }

        stmt.append(table);

        try(PreparedStatement prepared = stmt.prepare(connection)) {
            return prepared.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute DROP TABLE statment!", ex);
        }
    }
}
