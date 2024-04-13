package org.wallentines.mdcfg.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents a type of database
 */
public class DatabaseType {

    protected final String prefix;

    /**
     * Creates a database type with the given JDBC prefix
     * @param prefix The JDBC connection prefix.
     */
    public DatabaseType(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @return A new SQL connection
     */
    public SQLConnection create(String url) {
        return create(url, null, null);
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param username The database username
     * @param password The database password
     * @return A new SQL connection
     */
    public SQLConnection create(String url, String username, String password) {
        try {
            return new SQLConnection(this, DriverManager.getConnection("jdbc:" + prefix + url, username, password));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!", ex);
        }
    }

    /**
     * A factory for creating a new database type given a prefix
     */
    public interface Factory {
        DatabaseType create(String prefix);
        Factory DEFAULT = DatabaseType::new;
    }


}
