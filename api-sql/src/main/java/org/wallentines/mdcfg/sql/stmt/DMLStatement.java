package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.UpdateResult;

public abstract class DMLStatement extends Statement {


    protected DMLStatement(SQLConnection connection) {
        super(connection);
    }

    public abstract UpdateResult execute();

}
