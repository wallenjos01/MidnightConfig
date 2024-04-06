package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigSection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLConnection implements AutoCloseable {

    private final DatabaseType type;
    private final Connection internal;
    private String db;


    public SQLConnection(DatabaseType type, Connection internal) {
        this.type = type;
        this.internal = internal;
    }

    public boolean isConnected() {
        try {
            return internal != null && !internal.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    public DatabaseType getType() {
        return type;
    }

    @Nullable
    public String getActiveDatabase() {
        return db;
    }

    public Set<String> getTables() {

        Set<String> out = new HashSet<>();
        try {

            ResultSet set = internal.getMetaData().getTables(null, null, null, null);
            while(set.next()) {
                String table = set.getString("TABLE_NAME");
                out.add(table);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to query tables!", ex);
        }

        return out;
    }

    public boolean hasTable(String name) {
        if(type.getDialect().namesAreUppercase) {
            name = name.toUpperCase();
        }
        return getTables().contains(name);
    }

    public void createTable(String name, TableSchema schema) {
        if (!SQLUtil.VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + name);
        }
        execute(type.getDialect().createTable(name, schema));
    }

    public void setActiveDatabase(String db) {
        if(type.isSingleDB()) return;
        if (!SQLUtil.VALID_NAME.matcher(db).matches()) {
            throw new IllegalArgumentException("Invalid database name: " + db);
        }
        if(execute(type.getDialect().useDB(db)) > 0) {
            this.db = db;
        }
    }

    private int execute(String statement) {
        if(!isConnected()) {
            throw new IllegalStateException("Database is not connected!");
        }
        try {
            Statement out = internal.createStatement();
            out.closeOnCompletion();
            out.execute(statement);
            return out.getUpdateCount();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to execute SQL statement " + statement + "!", ex);
        }
    }

    public List<ConfigSection> select(String table, TableSchema schema, @Nullable Where where) {

        String query = type.getDialect().select(table, schema, where);
        List<ConfigSection> out = new ArrayList<>();
        try {

            ResultSet set = internal.createStatement().executeQuery(query);
            while(set.next()) {
                ConfigSection row = new ConfigSection();
                for (String key : schema.getColumnNames()) {
                    row.set(key, schema.getType(key).read(set, key));
                }
                out.add(row);
            }

        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to execute SQL statement " + query + "!", ex);
        }
        return out;
    }

    public List<ConfigSection> select(String table, TableSchema schema) {
        return select(table, schema, null);
    }


    public void insert(String table, TableSchema schema, ConfigSection row) {
        execute(type.getDialect().insert(table, schema, row));
    }

    public void update(String table, TableSchema schema, ConfigSection row, @Nullable Where where) {
        execute(type.getDialect().update(table, schema, row, where));
    }

    public void delete(String table, @Nullable Where where) {
        execute(type.getDialect().delete(table, where));
    }

    public void clearTable(String table) {
        delete(table, null);
    }

    public void dropTable(String table) {
        execute(type.getDialect().dropTable(table));
    }

    @Override
    public void close() {
        try {
            internal.close();
        } catch (SQLException ex) {
            // Ignore
        }
    }
}
