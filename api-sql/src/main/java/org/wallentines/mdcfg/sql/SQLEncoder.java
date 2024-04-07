package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class SQLEncoder {

    // CREATE TABLE name (key1 type1, key2 type2, ...)
    public PreparedStatement createTable(Connection conn, String name, TableSchema schema) throws SQLException {
        SQLUtil.validate(name);
        String stmt = "CREATE TABLE " + name + " (" +
                schema.getColumnNames().stream()
                        .map(key -> key + " " + schema.getType(key).getEncoded())
                        .collect(Collectors.joining(", "))
                + ");";

        return conn.prepareStatement(stmt);
    }

    // SELECT key1,key2,key3 FROM table WHERE ...;
    public PreparedStatement select(Connection conn, String table, TableSchema schema, @Nullable Where where) throws SQLException {
        SQLUtil.validate(table);
        StringBuilder query = new StringBuilder("SELECT ")
                .append(String.join(", ", schema.getColumnNames()))
                .append(" FROM ").append(table);

        if (where != null) {
            query.append(" WHERE ").append(where(where));
        }
        query.append(";");

        PreparedStatement stmt = conn.prepareStatement(query.toString());
        if(where != null) {
            where.writeArguments(stmt, 1);
        }

        return stmt;
    }


    // INSERT INTO table (key1, key2, key3) VALUES (val1, val2, val3);
    public PreparedStatement insert(Connection conn, String table, TableSchema schema, ConfigSection row) throws SQLException {
        SQLUtil.validate(table);
        StringBuilder out = new StringBuilder("INSERT INTO ")
                .append(table)
                .append("(")
                .append(String.join(", ", schema.getColumnNames()))
                .append(") VALUES (");

        for(int i = 0 ; i < schema.getColumnCount() ; i++) {
            if(i > 0) out.append(",");
            out.append("?");
        }
        out.append(");");
        PreparedStatement stmt = conn.prepareStatement(out.toString());

        for(int i = 0 ; i < schema.getColumnCount() ; i++) {
            String name = schema.getColumnName(i);
            schema.getType(i).getWriter().write(ConfigContext.INSTANCE, row.get(name), i+1, stmt);
        }

        return stmt;
    }

    // UPDATE table SET key1 = val1, key2 = val2, key3=val3 WHERE ...;
    public PreparedStatement update(Connection conn, String table, TableSchema schema, ConfigSection row, @Nullable Where where) throws SQLException {
        SQLUtil.validate(table);
        StringBuilder query = new StringBuilder("UPDATE " +
                table + " SET " +
                schema.getColumnNames().stream().map(key -> key + " = ?").collect(Collectors.joining(", ")));

        if (where != null) {
            query.append(" WHERE ").append(where(where));
        }
        query.append(";");
        PreparedStatement stmt = conn.prepareStatement(query.toString());

        for(int i = 0 ; i < schema.getColumnCount() ; i++) {
            String name = schema.getColumnName(i);
            schema.getType(i).getWriter().write(ConfigContext.INSTANCE, row.get(name), i+1, stmt);
        }

        if(where != null) {
            where.writeArguments(stmt, schema.getColumnCount() + 1);
        }

        return stmt;
    }

    public PreparedStatement delete(Connection conn, String table, @Nullable Where where) throws SQLException {
        SQLUtil.validate(table);
        StringBuilder out = new StringBuilder("DELETE FROM ").append(table);
        if(where != null) {
            out.append(" WHERE ").append(where(where));
        }
        out.append(";");
        PreparedStatement stmt = conn.prepareStatement(out.toString());

        if(where != null) {
            where.writeArguments(stmt, 1);
        }

        return stmt;
    }

    public PreparedStatement dropTable(Connection conn, String table) throws SQLException {
        SQLUtil.validate(table);
        return conn.prepareStatement("DROP TABLE " + table + ";");
    }

    private String where(Where where) {

        StringBuilder builder = new StringBuilder();
        if(where.isInverted()) {
            builder.append("NOT ");
        }
        builder.append(where.key);

        switch (where.operand) {
            case EQUALS: builder.append(" = ?"); break;
            case GREATER_THAN: builder.append(" > ?"); break;
            case LESS_THAN: builder.append(" < ?"); break;
            case AT_LEAST: builder.append(" >= ?"); break;
            case AT_MOST: builder.append(" <= ?"); break;
            case BETWEEN: builder.append(" BETWEEN ? AND ?"); break;
            case IN: {
                builder.append(" IN(");
                for(int i = 0 ; i < where.getArgumentCount() ; i++) {
                    if(i > 0) {
                        builder.append(',');
                    }
                    builder.append('?');
                }
                builder.append(')');
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

}
