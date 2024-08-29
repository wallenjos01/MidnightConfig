package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DropIndex extends DDLStatement {

    public final String indexName;
    public final String tableName;

    public DropIndex(SQLConnection connection, String indexName, String tableName) {
        super(connection);
        this.indexName = indexName;
        this.tableName = tableName;
    }


    @Override
    public boolean execute() {

        StatementBuilder stmt = new StatementBuilder().append("ALTER TABLE " + tableName + " DROP INDEX " + indexName + ";");
        try(PreparedStatement prepared = stmt.prepare(connection)) {
            return prepared.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute DROP INDEX statment!", ex);
        }
    }
}
