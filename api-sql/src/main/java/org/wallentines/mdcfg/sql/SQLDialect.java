package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;
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
            query.append(" WHERE ").append(where(where));
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
            query.append(" WHERE ").append(where(where));
        }
        query.append(";");
        return query.toString();
    }

    public String delete(String table, @Nullable Where where) {
        StringBuilder out = new StringBuilder("DELETE FROM ").append(table);
        if(where != null) {
            out.append(" WHERE ").append(where(where));
        }
        out.append(";");
        return out.toString();
    }

    public String dropTable(String table) {
        return "DROP TABLE " + table + ";";
    }

    public String where(Where where) {

        StringBuilder builder = new StringBuilder();
        if(where.isInverted()) {
            builder.append("NOT ");
        }
        builder.append(where.key);

        switch (where.operand) {
            case EQUALS: builder.append(" = ").append(where.writeArgument(0)); break;
            case GREATER_THAN: builder.append(" > ").append(where.writeArgument(0)); break;
            case LESS_THAN: builder.append(" < ").append(where.writeArgument(0)); break;
            case AT_LEAST: builder.append(" >= ").append(where.writeArgument(0)); break;
            case AT_MOST: builder.append(" <= ").append(where.writeArgument(0)); break;
            case BETWEEN: builder.append(" BETWEEN ")
                    .append(where.writeArgument(0))
                    .append(" AND ")
                    .append(where.writeArgument(1)); break;
            case IN: {
                builder.append(" IN(");
                for(int i = 0 ; i < where.getArgumentCount() ; i++) {
                    builder.append(where.writeArgument(i));
                }
                builder.append(")");
                break;
            }
        }

        for(Tuples.T2<Where.Conjunction, Where> child : where.children) {
            if(child.p1 == Where.Conjunction.AND) {
                builder.append(" AND (");
            } else {
                builder.append(" OR (");
            }
            builder.append(where(child.p2));
            builder.append(")");
        }

        return builder.toString();
    }


    public static final SQLDialect H2 = new SQLDialect(true);

}
