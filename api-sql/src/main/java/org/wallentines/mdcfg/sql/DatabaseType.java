package org.wallentines.mdcfg.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseType {

    private final String prefix;
    private final boolean singleDB;
    private final SQLDialect dialect;

    public DatabaseType(String prefix, boolean singleDB, SQLDialect dialect) {
        this.prefix = prefix;
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
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!");
        }
    }


    public static DatabaseType mysql() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new NoSuchDriverException("Could not find MySQL driver!", ex);
        }
        return new DatabaseType("mysql://", false, SQLDialect.STANDARD);
    }

    public static DatabaseType mariadb() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new NoSuchDriverException("Could not find MariaDB driver!", ex);
        }
        return new DatabaseType("mariadb://", false, SQLDialect.STANDARD);
    }

    public static DatabaseType sqlite() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new NoSuchDriverException("Could not find SQLite driver!", ex);
        }
        return new DatabaseType("sqlite:", true, new SQLDialect.SQLite());
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

        return new DatabaseType("h2:" + prefix, true, SQLDialect.STANDARD);
    }

    public enum ResourceType {
        MEMORY,
        FILE,
        TCP,
        SSL
    }

}
