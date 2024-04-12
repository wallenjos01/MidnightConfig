package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

public abstract class DMLStatement extends Statement {


    protected DMLStatement(SQLConnection connection) {
        super(connection);
    }

    public abstract int[] execute();

}
