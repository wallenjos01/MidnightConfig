package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.QueryResult;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Select extends DQLStatement {

    public final List<String> tables;
    public final List<Term> columns;
    public Expression where;
    private Term groupBy;

    private final List<String> joinClauses = new ArrayList<>(0);
    private final List<SortedTerm> orderBy = new ArrayList<>(0);

    public Select(SQLConnection connection, List<String> tables) {
        super(connection);
        this.tables = tables;
        this.columns = new ArrayList<>();
    }

    public Select withColumn(String column) {
        columns.add(Term.identifier(column));
        return this;
    }

    public Select withColumn(Term column) {
        columns.add(column);
        return this;
    }

    public Select withColumns(Collection<Term> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public Select where(Expression where) {
        this.where = where;
        return this;
    }

    public Select join(JoinType type, String otherTable, String column) {
        return join(type, tables.get(0), otherTable, column, column);
    }

    public Select join(JoinType type, String table, String otherTable, String column, String otherColumn) {
        this.joinClauses.add(type.keyword + " JOIN " +
                connection.applyPrefix(otherTable) + " ON " +
                table + "." + column + "=" +
                connection.applyPrefix(otherTable) + "." + otherColumn);
        return this;
    }

    public Select orderBy(String... columns) {
        return orderBy(SortOrder.ASCENDING, columns);
    }

    public Select orderBy(SortOrder order, String... columns) {
        for(String s : columns) {
            orderBy.add(new SortedTerm(Term.identifier(s), order));
        }
        return this;
    }

    public Select orderBy(List<SortedTerm> terms) {
        orderBy.addAll(terms);
        return this;
    }

    public Select groupBy(String column) {
        this.groupBy = Term.identifier(column);
        return this;
    }

    public Select groupBy(Term column) {
        this.groupBy = column;
        return this;
    }

    @Override
    public StatementBuilder toBuilder() {
        StatementBuilder query = new StatementBuilder("SELECT ");
        if(columns.isEmpty()) {
            query.append("*");
        } else {
            for(int i = 0 ; i < columns.size() ; i++) {
                if(i > 0) query.append(", ");
                query.appendTerm(columns.get(i));
            }
        }
        query.append(" FROM ").appendList(tables);

        for(String s : joinClauses) {
            query.append(" ").append(s);
        }
        if (where != null) {
            query.append(" WHERE ").appendExpression(where);
        }
        for(int i = 0 ; i < orderBy.size() ; i++) {
            if(i == 0) {
                query.append(" ORDER BY ");
            } else {
                query.append(", ");
            }

            SortedTerm term = orderBy.get(i);
            query.appendTerm(term.term);
            query.append(" " + term.order.keyword);
        }
        if(groupBy != null) {
            query.append(" GROUP BY ").appendTerm(groupBy);
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

    public static class SortedTerm {
        public final Term term;
        public final SortOrder order;

        public SortedTerm(Term term, SortOrder order) {
            this.term = term;
            this.order = order;
        }
    }

    public enum SortOrder {
        ASCENDING("ASC"),
        DESCENDING("DESC");

        final String keyword;
        SortOrder(String keyword) {
            this.keyword = keyword;
        }
    }

    public enum JoinType {
        INNER("INNER"),
        LEFT("LEFT"),
        RIGHT("RIGHT");

        final String keyword;
        JoinType(String keyword) {
            this.keyword = keyword;
        }
    }

}
