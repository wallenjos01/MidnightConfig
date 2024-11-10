package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

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

    public static TableConstraint<Tuples.T2<String, ColumnRef>> FOREIGN_KEY(String column, ColumnRef ref) {
        SQLUtil.validate(column);
        return new TableConstraint<>(Type.FOREIGN_KEY, new Tuples.T2<>(column, ref));
    }

    public static TableConstraint<Condition> CHECK(Condition condition) {
        return new TableConstraint<>(Type.CHECK, condition);
    }

    public enum Type {
        UNIQUE,
        CHECK,
        PRIMARY_KEY,
        FOREIGN_KEY
    }


}
