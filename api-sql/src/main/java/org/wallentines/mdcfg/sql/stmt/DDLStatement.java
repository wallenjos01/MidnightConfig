package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

public abstract class DDLStatement extends Statement {

    protected DDLStatement(SQLConnection connection) {
        super(connection);
    }

    public abstract boolean execute();

}
