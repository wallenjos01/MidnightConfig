package org.wallentines.mdcfg.sql.stmt;

import org.wallentines.mdcfg.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateIndex extends DDLStatement {

    private final String indexName;
    private final String tableName;

    private boolean unique;
    private final List<String> columns = new ArrayList<>();

    public CreateIndex(SQLConnection connection, String indexName, String tableName) {
        super(connection);
        this.indexName = indexName;
        this.tableName = tableName;
    }

    public CreateIndex unique() {
        this.unique = true;
        return this;
    }

    public CreateIndex withColumn(String column) {
        this.columns.add(column);
        return this;
    }

    public CreateIndex withColumns(String... columns) {
        this.columns.addAll(List.of(columns));
        return this;
    }

    @Override
    public boolean execute() {

        StringBuilder start = new StringBuilder("CREATE ");
        if(unique) start.append("UNIQUE ");
        start.append("INDEX ")
                .append(indexName)
                .append(" ON ")
                .append(tableName)
                .append("(");

        try(PreparedStatement stmt = new StatementBuilder()
                    .append(start.toString())
                    .appendList(columns)
                    .append(")")
                    .prepare(connection)) {

            return stmt.execute();

        } catch (SQLException ex) {
            return false;
        }
    }
}
