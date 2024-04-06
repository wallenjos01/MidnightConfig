package org.wallentines.mdcfg.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseType {

    private final String prefix;
    private final String driverClass;
    private final boolean singleDB;
    private final SQLDialect dialect;

    public DatabaseType(String prefix, String driverClass, boolean singleDB, SQLDialect dialect) {
        this.prefix = prefix;
        this.driverClass = driverClass;
        this.singleDB = singleDB;
        this.dialect = dialect;
    }

    public boolean isSingleDB() {
        return singleDB;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public SQLConnection create(String url) {
        try {
            return new SQLConnection(this, DriverManager.getConnection("jdbc:" + prefix + url));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!", ex);
        }
    }

    public SQLConnection create(String url, String username, String password) {
        try {
            return new SQLConnection(this, DriverManager.getConnection("jdbc:" + prefix + url, username, password));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!", ex);
        }
    }


    public static final DatabaseType MYSQL = new DatabaseType("mysql://", "com.mysql.cj.jdbc.Driver", false, SQLDialect.STANDARD);
    public static final DatabaseType MARIADB = new DatabaseType("mariadb://", "org.mariadb.jdbc.Driver", false, SQLDialect.STANDARD);
    public static final DatabaseType SQLITE = new DatabaseType("sqlite:", "org.sqlite.JDBC", true, SQLDialect.STANDARD);
    public static final DatabaseType H2 = new DatabaseType("h2:file:", "org.h2.Driver", true, SQLDialect.STANDARD);


    public static DatabaseType mariadb() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new NoSuchDriverException("Could not find MariaDB driver!", ex);
        }
        return MARIADB;
    }

    public static DatabaseType sqlite() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new NoSuchDriverException("Could not find SQLite driver!", ex);
        }
        return SQLITE;
    }

    public static DatabaseType h2(ResourceType type) {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            throw new NoSuchDriverException("Could not find H2 driver!", ex);
        }
        String prefix = "";
        switch (type) {
            case MEMORY:
                prefix = "mem:";
                break;
            case FILE:
                prefix = "file:";
                break;
            case TCP:
                prefix = "tcp://";
                break;
            case SSL:
                prefix = "ssl://";
                break;
        }

        return new DatabaseType("h2:" + prefix, "org.h2.Driver", true, SQLDialect.H2);
    }

    public enum ResourceType {
        MEMORY,
        FILE,
        TCP,
        SSL
    }

}
