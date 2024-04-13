package org.wallentines.mdcfg.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseType {

    protected final String prefix;

    public DatabaseType(String prefix) {
        this.prefix = prefix;
    }

    public SQLConnection create(String url) {
        return create(url, null, null);
    }

    public SQLConnection create(String url, String username, String password) {
        try {
            return new SQLConnection(this, DriverManager.getConnection("jdbc:" + prefix + url, username, password));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!", ex);
        }
    }

    public interface Factory {
        DatabaseType create(String prefix);
        Factory DEFAULT = DatabaseType::new;
    }


}
