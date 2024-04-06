package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.util.stream.Collectors;

public class SQLDialect {

    public static final SQLDialect STANDARD = new SQLDialect(false);

    public final boolean namesAreUppercase;

    protected SQLDialect(boolean namesAreUppercase) {
        this.namesAreUppercase = namesAreUppercase;
    }


    public String createTable(String name, TableSchema schema) {
        return "CREATE TABLE " + name + "(" +
                schema.getColumnNames().stream()
                        .map(key -> key + " " + schema.getType(key).getEncoded())
                        .collect(Collectors.joining(", ")) +
                ");";
    }

    public String useDB(String db) {
        return "USE " + db + ";";
    }

    // SELECT key1,key2,key3 FROM table WHERE ...;
    public String select(String table, TableSchema schema, @Nullable Where where) {
        StringBuilder query = new StringBuilder("SELECT ")
                .append(String.join(", ", schema.getColumnNames()))
                .append(" FROM ").append(table);

        if (where != null) {
            query.append(" ").append(where);
        }
        query.append(";");
        return query.toString();
    }


    // INSERT INTO table (key1, key2, key3) VALUES (val1, val2, val3);
    public String insert(String table, TableSchema schema, ConfigSection row) {
        return "INSERT INTO " +
                table + "(" +
                String.join(", ", schema.getColumnNames()) +
                ") VALUES (" +
                schema.getColumnNames().stream().map(key -> {
                    ConfigObject obj = row.get(key);
                    if (obj == null) throw new IllegalArgumentException("Row missing required value " + key + "!");
                    return schema.getType(key).getWriter().write(ConfigContext.INSTANCE, obj);
                }).collect(Collectors.joining(", ")) +
                ");";
    }

    // UPDATE table SET key1 = val1, key2 = val2, key3=val3 WHERE ...;
    public String update(String table, TableSchema schema, ConfigSection row, @Nullable Where where) {
        StringBuilder query = new StringBuilder("UPDATE " +
                table + " SET " +
                schema.getColumnNames().stream().map(key -> {
                    ConfigObject obj = row.get(key);
                    if (obj == null) throw new IllegalArgumentException("Row missing required value " + key + "!");
                    return key + " = " + schema.getType(key).getWriter().write(ConfigContext.INSTANCE, obj);
                }).collect(Collectors.joining(", ")));

        if (where != null) {
            query.append(" ").append(where);
        }
        query.append(";");
        return query.toString();
    }

    public String delete(String table, @Nullable Where where) {
        StringBuilder out = new StringBuilder("DELETE FROM ").append(table);
        if(where != null) {
            out.append(" ").append(where);
        }
        out.append(";");
        return out.toString();
    }

    public String dropTable(String table) {
        return "DROP TABLE " + table + ";";
    }


    public static final SQLDialect H2 = new SQLDialect(true);

}
