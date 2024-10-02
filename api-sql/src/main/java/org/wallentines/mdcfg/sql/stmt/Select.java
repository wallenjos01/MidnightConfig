package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.sql.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Select extends DQLStatement {

    public final String table;
    public final List<String> columns;
    public Condition where;

    private String joinClause;

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

    public Select where(Condition where) {
        this.where = where;
        return this;
    }

    public Select join(JoinType type, String otherTable, String column) {
        return join(type, otherTable, column, column);
    }

    public Select join(JoinType type, String otherTable, String column, String otherColumn) {
        this.joinClause = type.keyword + " JOIN " + table + " ON " + table + "." + column + "=" + otherTable + "." + otherColumn;
        return this;
    }

    @Override
    public QueryResult execute() {

        StatementBuilder query = new StatementBuilder("SELECT ");
        if(columns.isEmpty()) {
            query.append("*");
        } else {
            query.append(String.join(", ", columns));
        }
        query.append(" FROM ").append(table);

        if (where != null) {
            query.append(" WHERE ").appendCondition(where);
        }
        if(joinClause != null) {
            query.append(" ").append(joinClause);
        }

        try(PreparedStatement stmt = query.prepare(connection)) {

            ResultSet set = stmt.executeQuery();
            int cols = set.getMetaData().getColumnCount();

            QueryResult res = new QueryResult(connection);
            while(set.next()) {
                List<Tuples.T2<String, DataValue<?>>> values = new ArrayList<>();
                for(int i = 1 ; i <= cols ; i++) {

                    int type = set.getMetaData().getColumnType(i);
                    if(type == Types.NULL) {
                        continue;
                    }

                    DataType<?> dt = DataType.get(type);
                    if(dt == null) {
                        throw new IllegalStateException("Unknown column type " + type + "!");
                    }

                    values.add(new Tuples.T2<>(set.getMetaData().getColumnName(i), dt.read(set, i)));
                }
                res.addRow(values);
            }
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
