package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.Expression;
import org.wallentines.mdcfg.sql.stmt.StatementBuilder;

import java.util.List;

public interface SQLDialect {

    StatementBuilder writeTableSchema(SQLConnection connection, TableSchema schema, String table);

    String writeColumnType(ColumnType<?> type);


    class Standard implements SQLDialect {
        protected void writeConstraint(SQLConnection conn, Column def, Constraint<?> constraint, StatementBuilder column, StatementBuilder table) {

            switch (constraint.type) {
                case NOT_NULL: column.append(" NOT NULL"); break;
                case AUTO_INCREMENT: column.append(" AUTO_INCREMENT"); break;
                case UNIQUE: column.append(" UNIQUE"); break;
                case PRIMARY_KEY: table.append(", PRIMARY KEY (").append(def.getName()).append(")"); break;
                case FOREIGN_KEY: {

                    ColumnRef ref = (ColumnRef) constraint.param;
                    if(ref.applyTablePrefix) {
                        ref = ref.withPrefix(conn.tablePrefix);
                    }
                    table.append(", FOREIGN KEY (").append(def.getName()).append(") REFERENCES ").append(ref.encode());
                    break;
                }
                case CHECK: {
                    Condition check = (Condition) constraint.param;
                    column.append(" CHECK(");
                    check.encode(column);
                    column.append(")");
                    break;
                }
                case DEFAULT: {

                    DataValue<?> val = (DataValue<?>) constraint.param;
                    column.append(" DEFAULT ").appendValue(val);
                    break;
                }
            }

        }

        @SuppressWarnings("unchecked")
        protected void writeTableConstraint(SQLConnection conn, String tableName, TableConstraint<?> constraint, StatementBuilder table) {

            switch (constraint.type) {
                case UNIQUE: {
                    table.append("UNIQUE(").appendList((List<String>) constraint.param).append(")");
                    break;
                }
                case PRIMARY_KEY: {
                    table.append("PRIMARY KEY(").appendList((List<String>) constraint.param).append(")");
                    break;
                }
                case FOREIGN_KEY: {

                    TableConstraint.ReferenceConstraint refConstraint = (TableConstraint.ReferenceConstraint) constraint;
                    ColumnRef ref = refConstraint.param;
                    String column = refConstraint.column;

                    if(ref.applyTablePrefix) {
                        ref = ref.withPrefix(conn.tablePrefix);
                    }

                    table.append("FOREIGN KEY(").append(column).append(") REFERENCES ").append(ref.encode());
                    if(refConstraint.deleteAction != null) {
                        table.append(" ON DELETE ").append(refConstraint.deleteAction.grammar);
                    }
                    if(refConstraint.updateAction != null) {
                        table.append(" ON UPDATE ").append(refConstraint.updateAction.grammar);
                    }
                    break;
                }
                case CHECK: {
                    Expression check = (Expression) constraint.param;
                    table.append("CHECK(").appendExpression(check).append(")");
                    break;
                }
            }

        }

        protected void writeColumn(SQLConnection conn, Column def, StatementBuilder column, StatementBuilder table) {
            column.append(def.getName()).append(" ").append(writeColumnType(def.getType()));
            for(Constraint<?> con : def.getConstraints()) {
                writeConstraint(conn, def, con, column, table);
            }
        }

        @Override
        public StatementBuilder writeTableSchema(SQLConnection connection, TableSchema schema, String table) {

            StatementBuilder out = new StatementBuilder();
            StatementBuilder postTable = new StatementBuilder();
            int index = 0;
            for(Column col : schema.getColumns()) {
                if(index++ > 0) {
                    out.append(", ");
                }
                writeColumn(connection, col, out, postTable);
            }

            for(TableConstraint<?> constraint : schema.getConstraints()) {
                postTable.append(", ");
                writeTableConstraint(connection, table, constraint, postTable);
            }

            out.append(postTable);
            return out;
        }

        @Override
        public String writeColumnType(ColumnType<?> type) {
            return type.getDataType().getName() + type.getParameters();
        }

    }

    class MySQL extends Standard {
        @Override
        public String writeColumnType(ColumnType<?> type) {

            if(type.getDataType() == DataType.LONGVARCHAR) {
                return "MEDIUMTEXT";
            }
            return super.writeColumnType(type);
        }
    }

    Standard STANDARD = new Standard();
    MySQL MYSQL = new MySQL();


}
