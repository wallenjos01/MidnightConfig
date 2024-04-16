package org.wallentines.mdcfg.sql;

import java.util.stream.Collectors;

public interface SQLDialect {

    String writeTableSchema(SQLConnection connection, TableSchema schema);


    class Standard implements SQLDialect {
        protected void writeConstraint(SQLConnection conn, Column def, Constraint<?> constraint, StringBuilder column, StringBuilder table) {

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
                    Condition ref = (Condition) constraint.param;
                    column.append(" CHECK(").append(ref.encode()).append(")");
                    // TODO: Apply data value
                    break;
                }
                case DEFAULT: column.append(" DEFAULT ?"); {
                    // TODO: Apply data value
                    break;
                }
            }

        }

        protected void writeColumn(SQLConnection conn, Column def, StringBuilder column, StringBuilder table) {
            column.append(def.getName()).append(" ").append(def.getType().getEncoded());
            for(Constraint<?> con : def.getConstraints()) {
                writeConstraint(conn, def, con, column, table);
            }
        }

        @Override
        public String writeTableSchema(SQLConnection connection, TableSchema schema) {

            StringBuilder postTable = new StringBuilder();
            return schema.getColumns().stream().map(col -> {

                StringBuilder out = new StringBuilder();
                writeColumn(connection, col, out, postTable);

                return out.toString();
            }).collect(Collectors.joining(", ")) + postTable;
        }
    }

    class SQLite extends Standard {

        @Override
        protected void writeConstraint(SQLConnection conn, Column def, Constraint<?> constraint, StringBuilder column, StringBuilder table) {
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

    Standard STANDARD = new Standard();
    SQLite SQLITE = new SQLite();


}
