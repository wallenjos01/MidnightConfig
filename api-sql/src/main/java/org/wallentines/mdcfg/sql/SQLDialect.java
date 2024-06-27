package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.StatementBuilder;

public interface SQLDialect {

    StatementBuilder writeTableSchema(SQLConnection connection, TableSchema schema);

    String writeColumnType(ColumnType<?> type);


    class Standard implements SQLDialect {
        protected void writeConstraint(SQLConnection conn, Column def, Constraint<?> constraint, StatementBuilder column, StatementBuilder table) {

            switch (constraint.type) {
                case NOT_NULL: column.append(" NOT NULL"); break;
                case AUTO_INCREMENT: column.append(" AUTO_INCREMENT");
                case UNIQUE: column.append(" UNIQUE"); break;
                case PRIMARY_KEY: table.append(", PRIMARY KEY(").append(def.getName()).append(")"); break;
                case FOREIGN_KEY: {

                    ColumnRef ref = (ColumnRef) constraint.param;
                    if(ref.applyTablePrefix) {
                        ref = ref.withPrefix(conn.tablePrefix);
                    }
                    table.append(", FOREIGN_KEY(").append(def.getName()).append(") REFERENCES").append(ref.encode());
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

        protected void writeColumn(SQLConnection conn, Column def, StatementBuilder column, StatementBuilder table) {
            column.append(def.getName()).append(" ").append(writeColumnType(def.getType()));
            for(Constraint<?> con : def.getConstraints()) {
                writeConstraint(conn, def, con, column, table);
            }
        }

        @Override
        public StatementBuilder writeTableSchema(SQLConnection connection, TableSchema schema) {

            StatementBuilder out = new StatementBuilder();
            StatementBuilder postTable = new StatementBuilder();
            int index = 0;
            for(Column col : schema.getColumns()) {
                if(index++ > 0) {
                    out.append(",");
                }
                writeColumn(connection, col, out, postTable);
            }

            out.append(postTable);
            return out;
        }

        @Override
        public String writeColumnType(ColumnType<?> type) {
            return type.getDataType().getName() + type.getParameters();
        }

    }

    class SQLite extends Standard {

        @Override
        protected void writeConstraint(SQLConnection conn, Column def, Constraint<?> constraint, StatementBuilder column, StatementBuilder table) {
            if(constraint.type == Constraint.Type.PRIMARY_KEY) {
                column.append(" PRIMARY KEY");
                return;
            }
            if(constraint.type == Constraint.Type.AUTO_INCREMENT) {
                column.append(" AUTOINCREMENT");
                return;
            }
            super.writeConstraint(conn, def, constraint, column, table);
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
    SQLite SQLITE = new SQLite();
    MySQL MYSQL = new MySQL();


}
