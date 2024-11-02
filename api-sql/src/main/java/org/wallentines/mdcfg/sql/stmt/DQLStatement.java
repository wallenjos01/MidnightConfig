package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.QueryResult;
import org.wallentines.mdcfg.sql.SQLConnection;

public abstract class DQLStatement extends Statement {

    protected DQLStatement(SQLConnection connection) {
        super(connection);
    }

    public abstract StatementBuilder toBuilder();

    public abstract QueryResult execute();

}
