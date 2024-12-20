package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.SQLUtil;
import org.wallentines.mdcfg.sql.UpdateResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Delete extends DMLStatement {

    public final String table;
    public Expression where;

    public Delete(SQLConnection connection, String table) {
        super(connection);
        SQLUtil.validate(table);
        this.table = table;
    }

    public Delete where(Expression where) {
        this.where = where;
        return this;
    }

    @Override
    public UpdateResult execute() {

        StatementBuilder out = new StatementBuilder().append("DELETE FROM " + table);

        if(where != null) {
            out.append(" WHERE ").appendExpression(where);
        }

        try(PreparedStatement stmt = out.prepare(connection)) {
            return new UpdateResult(new int[] { stmt.executeUpdate() }, null);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute DELETE statement!", ex);
        }
    }
}
