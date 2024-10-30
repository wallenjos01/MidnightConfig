package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.Condition;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.SQLUtil;
import org.wallentines.mdcfg.sql.UpdateResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Delete extends DMLStatement {

    public final String table;
    public Condition where;
    private final List<String> joinClauses = new ArrayList<>();

    public Delete(SQLConnection connection, String table) {
        super(connection);
        SQLUtil.validate(table);
        this.table = table;
    }

    public Delete where(Condition where) {
        this.where = where;
        return this;
    }

    public Delete join(Select.JoinType type, String otherTable, String column) {
        return join(type, otherTable, column, column);
    }

    public Delete join(Select.JoinType type, String otherTable, String column, String otherColumn) {
        this.joinClauses.add(type.keyword + " JOIN " +
                connection.applyPrefix(otherTable) + " ON " +
                table + "." + column + "=" +
                connection.applyPrefix(otherTable) + "." + otherColumn);
        return this;
    }

    @Override
    public UpdateResult execute() {

        StatementBuilder out = new StatementBuilder().append("DELETE FROM " + table);
        for(String s : joinClauses) {
            out.append(" ").append(s);
        }
        if(where != null) {
            out.append(" WHERE ").appendCondition(where);
        }

        try(PreparedStatement stmt = out.prepare(connection)) {
            return new UpdateResult(new int[] { stmt.executeUpdate() }, null);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute DELETE statement!");
        }
    }
}
