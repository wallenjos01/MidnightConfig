package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Select extends DQLStatement {

    public final String table;
    public final List<String> columns;
    public Expression where;

    private final List<String> joinClauses = new ArrayList<>();

    public Select(SQLConnection connection, String table) {
        super(connection);

        SQLUtil.validate(table);
        this.table = table;
        this.columns = new ArrayList<>();
    }

    public Select withColumn(String column) {
        columns.add(column);
        return this;
    }

    public Select withColumns(Collection<String> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public Select where(Expression where) {
        this.where = where;
        return this;
    }

    public Select join(JoinType type, String otherTable, String column) {
        return join(type, otherTable, column, column);
    }

    public Select join(JoinType type, String otherTable, String column, String otherColumn) {
        this.joinClauses.add(type.keyword + " JOIN " +
                connection.applyPrefix(otherTable) + " ON " +
                table + "." + column + "=" +
                connection.applyPrefix(otherTable) + "." + otherColumn);
        return this;
    }

    @Override
    public StatementBuilder toBuilder() {
        StatementBuilder query = new StatementBuilder("SELECT ");
        if(columns.isEmpty()) {
            query.append("*");
        } else {
            query.append(String.join(", ", columns));
        }
        query.append(" FROM ").append(table);

        for(String s : joinClauses) {
            query.append(" ").append(s);
        }
        if (where != null) {
            query.append(" WHERE ").appendExpression(where);
        }
        return query;
    }

    @Override
    public QueryResult execute() {

        try(PreparedStatement stmt = toBuilder().prepare(connection)) {

            ResultSet set = stmt.executeQuery();
            QueryResult res = QueryResult.fromResultSet(set, connection);

            stmt.close();

            return res;
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute SELECT statement!", ex);
        }
    }

    public enum JoinType {
        INNER("INNER"),
        LEFT("LEFT"),
        RIGHT("RIGHT"),
        FULL("FULL OUTER");

        final String keyword;
        JoinType(String keyword) {
            this.keyword = keyword;
        }
    }

}
