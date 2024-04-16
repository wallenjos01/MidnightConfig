package org.wallentines.mdcfg.sql;

/**
 * Represents a column constraint
 */
public class Constraint<T> {

    public final Type type;
    public final T param;
    public Constraint(Type type, T param) {
        this.type = type;
        this.param = param;
    }


    public static final Constraint<Void> NOT_NULL = new Constraint<>(Type.NOT_NULL, null);
    public static final Constraint<Void> UNIQUE = new Constraint<>(Type.UNIQUE, null);
    public static final Constraint<Void> PRIMARY_KEY = new Constraint<>(Type.PRIMARY_KEY, null);
    public static final Constraint<Void> AUTO_INCREMENT =  new Constraint<>(Type.AUTO_INCREMENT, null);
    public static Constraint<ColumnRef> FOREIGN_KEY(ColumnRef ref) {
        return new Constraint<>(Type.FOREIGN_KEY, ref);
    }
    public static Constraint<Condition> CHECK(Condition condition) {
        return new Constraint<>(Type.CHECK, condition);
    }
    public static <D> Constraint<DataValue<D>> DEFAULT(DataValue<D> value) {
        return new Constraint<>(Type.DEFAULT, value);
    }


    public enum Type {

        NOT_NULL,
        UNIQUE,
        PRIMARY_KEY,
        AUTO_INCREMENT,
        FOREIGN_KEY,
        CHECK,
        DEFAULT

    }


}
