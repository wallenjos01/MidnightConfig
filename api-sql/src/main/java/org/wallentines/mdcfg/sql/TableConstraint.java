package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

import java.util.Collection;
import java.util.List;

public class TableConstraint<T> {

    public final Type type;
    public final String name;
    public final T param;

    public TableConstraint(Type type, String name, T param) {
        SQLUtil.validate(name);
        this.type = type;
        this.name = name;
        this.param = param;
    }

    public static TableConstraint<List<String>> UNIQUE(String name, Collection<String> columns) {
        assert !columns.isEmpty();
        return new TableConstraint<>(Type.UNIQUE, name, List.copyOf(columns));
    }

    public static TableConstraint<List<String>> PRIMARY_KEY(String name, Collection<String> columns) {
        assert !columns.isEmpty();
        return new TableConstraint<>(Type.PRIMARY_KEY, name, List.copyOf(columns));
    }

    public static TableConstraint<Tuples.T2<String, ColumnRef>> FOREIGN_KEY(String name, String column, ColumnRef ref) {
        SQLUtil.validate(column);
        return new TableConstraint<>(Type.FOREIGN_KEY, name, new Tuples.T2<>(column, ref));
    }

    public static TableConstraint<Condition> CHECK(String name, Condition condition) {
        return new TableConstraint<>(Type.CHECK, name, condition);
    }

    public enum Type {
        UNIQUE,
        CHECK,
        PRIMARY_KEY,
        FOREIGN_KEY
    }


}
