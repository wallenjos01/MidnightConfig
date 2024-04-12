package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DropTable extends DDLStatement {

    public final String table;

    public DropTable(SQLConnection connection, String table) {
        super(connection);
        this.table = table;
    }

    @Override
    public boolean execute() {

        try(PreparedStatement stmt = connection.getInternal().prepareStatement("DROP TABLE " + table + ";")) {
            return stmt.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute DROP TABLE statment!");
        }
    }
}
