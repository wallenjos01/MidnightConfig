package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.Expression;

import java.util.Collection;
import java.util.List;

public class TableConstraint<T> {

    public final Type type;
    public final T param;

    public TableConstraint(Type type, T param) {
        this.type = type;
        this.param = param;
    }

    public static TableConstraint<List<String>> UNIQUE(Collection<String> columns) {
        assert !columns.isEmpty();
        return new TableConstraint<>(Type.UNIQUE, List.copyOf(columns));
    }

    public static TableConstraint<List<String>> UNIQUE(String... columns) {
        assert columns.length > 0;
        return new TableConstraint<>(Type.UNIQUE, List.of(columns));
    }

    public static TableConstraint<List<String>> PRIMARY_KEY(Collection<String> columns) {
        assert !columns.isEmpty();
        return new TableConstraint<>(Type.PRIMARY_KEY, List.copyOf(columns));
    }

    public static TableConstraint<List<String>> PRIMARY_KEY(String... columns) {
        assert columns.length > 0;
        return new TableConstraint<>(Type.PRIMARY_KEY, List.of(columns));
    }

    public static ReferenceConstraint FOREIGN_KEY(String column, ColumnRef ref) {
        SQLUtil.validate(column);
        return new ReferenceConstraint(column, ref);
    }

    public static TableConstraint<Expression> CHECK(Expression condition) {
        return new TableConstraint<>(Type.CHECK, condition);
    }


    public static class ReferenceConstraint extends TableConstraint<ColumnRef> {

        public final String column;
        public boolean cascade = false;

        public ReferenceConstraint(String column, ColumnRef ref) {
            super(Type.FOREIGN_KEY, ref);
            this.column = column;
        }

        public ReferenceConstraint cascade() {
            this.cascade = true;
            return this;
        }
    }

    public enum Type {
        UNIQUE,
        CHECK,
        PRIMARY_KEY,
        FOREIGN_KEY
    }


}
