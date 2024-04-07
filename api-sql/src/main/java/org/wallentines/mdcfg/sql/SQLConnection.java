package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLConnection implements AutoCloseable {

    private final DatabaseType type;
    private final Connection internal;
    private final SQLEncoder encoder;

    public SQLConnection(DatabaseType type, Connection internal, SQLEncoder encoder) {
        this.type = type;
        this.internal = internal;
        this.encoder = encoder;
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
        if(type.namesAreUppercase) {
            name = name.toUpperCase();
        }
        return getTables().contains(name);
    }


    public void createTable(String name, TableSchema schema) {
        if(!isConnected()) {
            throw new IllegalStateException("Database is not connected!");
        }

        try {
            PreparedStatement stmt = encoder.createTable(internal, name, schema);
            stmt.execute();

        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to create table!", ex);
        }
    }

    public List<ConfigSection> select(String table, TableSchema schema, @Nullable Where where) {

        List<ConfigSection> out = new ArrayList<>();
        try {

            PreparedStatement stmt = encoder.select(internal, table, schema, where);
            ResultSet set = stmt.executeQuery();

            while(set.next()) {
                ConfigSection row = new ConfigSection();
                for (String key : schema.getColumnNames()) {
                    SQLDataValue<ConfigObject> obj = schema.getType(key).read(ConfigContext.INSTANCE, set, key);
                    row.set(key, obj.getValue());
                }
                out.add(row);
            }

        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to select from table " + table + "!", ex);
        }
        return out;
    }

    public List<ConfigSection> select(String table, TableSchema schema) {
        return select(table, schema, null);
    }


    public void insert(String table, TableSchema schema, ConfigSection row) {

        try {
            encoder.insert(internal, table, schema, row).execute();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to insert into table " + table + "!", ex);
        }
    }

    public void update(String table, TableSchema schema, ConfigSection row, @Nullable Where where) {
        try {
            encoder.update(internal, table, schema, row, where).execute();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to update table " + table + "!", ex);
        }
    }

    public void delete(String table, @Nullable Where where) {
        try {
            encoder.delete(internal, table, where).execute();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to delete from table " + table + "!", ex);
        }
    }

    public void clearTable(String table) {
        delete(table, null);
    }

    public void dropTable(String table) {
        try {
            encoder.dropTable(internal, table).execute();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to drop table " + table + "!", ex);
        }
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
