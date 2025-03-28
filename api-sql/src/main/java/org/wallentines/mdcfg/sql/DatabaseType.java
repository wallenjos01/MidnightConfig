package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Represents a type of database
 */
public class DatabaseType {

    protected final String prefix;
    protected final SQLDialect dialect;

    /**
     * Creates a database type with the given JDBC prefix
     * @param prefix The JDBC connection prefix.
     */
    public DatabaseType(String prefix, SQLDialect dialect) {
        this.prefix = prefix;
        this.dialect = dialect;
    }

    /**
     * Gets a reference to the SQL dialect corresponding to this database type
     * @return The SQL dialect for this database type
     */
    public SQLDialect getDialect() {
        return dialect;
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @return A new SQL connection
     */
    public SQLConnection create(String url) {
        return create(url, null, null, null, new ConfigSection());
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param config Properties to be passed to the JDBC driver
     * @return A new SQL connection
     */
    public SQLConnection create(String url, ConfigSection config) {
        return create(url, null, null, null, config);
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param username The database username
     * @param password The database password
     * @return A new SQL connection
     */
    public SQLConnection create(String url, String username, String password) {
        return create(url, username, password, null, new ConfigSection());
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param username The database username
     * @param password The database password
     * @param tablePrefix A prefix to append to all table names
     * @param config Properties to be passed to the JDBC driver
     * @return A new SQL connection
     */
    public SQLConnection create(String url, String username, String password, @Nullable String tablePrefix, ConfigSection config) {

        return create(url, username, password, tablePrefix, config, Paths.get(System.getProperty("user.dir")));
    }

    /**
     * Connects to the database at the given URL
     * @param url The address/path database to connect to
     * @param username The database username
     * @param password The database password
     * @param tablePrefix A prefix to append to all table names
     * @param config Properties to be passed to the JDBC driver
     * @param resolveFrom The root directory to resolve database files based on, if applicable.
     * @return A new SQL connection
     */
    public SQLConnection create(String url, String username, String password, @Nullable String tablePrefix, ConfigSection config, Path resolveFrom) {

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
            return new SQLConnection(this, DriverManager.getConnection(getConnectionString(url, resolveFrom), properties), tablePrefix);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!", ex);
        }
    }

    protected String getConnectionString(String url, Path resolveFrom) {
        return getConnectionString(url);
    }

    protected String getConnectionString(String url) {
        return "jdbc:" + prefix + url;
    }

    /**
     * A factory for creating a new database type given a prefix
     */
    public interface Factory {
        DatabaseType create(String prefix);
        Factory DEFAULT = pre -> (new DatabaseType(pre, SQLDialect.STANDARD));
        Factory MYSQL = pre -> (new DatabaseType(pre, SQLDialect.MYSQL));
        Factory H2 = H2::new;
    }

    private static class H2 extends DatabaseType {
        public H2(String prefix) {
            super(prefix, SQLDialect.STANDARD);
        }

        @Override
        protected String getConnectionString(String url, Path resolveFrom) {

            if(url.startsWith("tcp://") ||
                    url.startsWith("ssl://") ||
                    url.startsWith(".") ||
                    url.startsWith("mem:")
            ) return super.getConnectionString(url);

            String file = url;
            if(file.startsWith("file:")) file = file.substring(5);

            return super.getConnectionString("file:" + resolveFrom.resolve(file).toAbsolutePath());
        }
    }


}
