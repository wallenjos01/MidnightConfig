package org.wallentines.mdcfg.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseType {

    private final String prefix;
    private final String driverClass;
    private final SQLDialect dialect;

    public DatabaseType(String prefix, String driverClass, SQLDialect dialect) {
        this.prefix = prefix;
        this.driverClass = driverClass;
        this.dialect = dialect;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public SQLConnection create(String url) {
        return create(url, null, null);
    }

    public SQLConnection create(String url, String username, String password) {
        try {
            Class.forName(driverClass);
            return new SQLConnection(this, DriverManager.getConnection("jdbc:" + prefix + url, username, password));
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Unable to load database driver!", ex);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to connect to database with URL " + prefix + url + "!", ex);
        }
    }


    public static final DatabaseType MYSQL = new DatabaseType("mysql://", "com.mysql.cj.jdbc.Driver", SQLDialect.STANDARD);
    public static final DatabaseType MARIADB = new DatabaseType("mariadb://", "org.mariadb.jdbc.Driver", SQLDialect.STANDARD);
    public static final DatabaseType SQLITE = new DatabaseType("sqlite:", "org.sqlite.JDBC", SQLDialect.STANDARD);
    public static final DatabaseType H2 = new DatabaseType("h2:", "org.h2.Driver", SQLDialect.H2);


}
