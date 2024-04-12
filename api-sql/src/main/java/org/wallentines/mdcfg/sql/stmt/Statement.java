package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

public abstract class Statement {

    protected final SQLConnection connection;
    protected Statement(SQLConnection connection) {
        if(!connection.isConnected()) {
            throw new IllegalArgumentException("Connection is not connected!");
        }
        this.connection = connection;
    }

}
