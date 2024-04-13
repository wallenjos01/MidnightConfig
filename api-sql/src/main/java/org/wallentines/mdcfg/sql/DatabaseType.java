package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
        return create(url, null, null, new ConfigSection());
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param config Properties to be passed to the JDBC driver
     * @return A new SQL connection
     */
    public SQLConnection create(String url, ConfigSection config) {
        return create(url, null, null, config);
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param username The database username
     * @param password The database password
     * @return A new SQL connection
     */
    public SQLConnection create(String url, String username, String password) {
        return create(url, username, password, new ConfigSection());
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param username The database username
     * @param password The database password
     * @param config Properties to be passed to the JDBC driver
     * @return A new SQL connection
     */
    public SQLConnection create(String url, String username, String password, ConfigSection config) {

        Properties properties = new Properties();
        for(String s : config.getKeys()) {
            ConfigObject obj = config.get(s);
            if(obj == null || !obj.isPrimitive()) {
                throw new IllegalArgumentException("Found invalid property in config: [" + s + " = " + obj + "]");
            }
            properties.put(s, obj.asPrimitive().getValue());
        }
        if(username != null) properties.put("user", username);
        if(password != null) properties.put("password", password);

        try {
            return new SQLConnection(this, DriverManager.getConnection("jdbc:" + prefix + url, properties));
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
