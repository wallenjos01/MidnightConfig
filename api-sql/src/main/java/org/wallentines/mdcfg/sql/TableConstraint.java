package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.Expression;

import java.util.Collection;
import java.util.List;

/**
 * Represents a constraint applied to a whole table
 * @param <T> The constraint argument
 */
public class TableConstraint<T> {

    public final Type type;
    public final T param;

    /**
     * Creates a new constraint with the given type and parameter
     * @param type The type of constraint
     * @param param The constraint argument
     */
    public TableConstraint(Type type, T param) {
        this.type = type;
        this.param = param;
    }

    /**
     * Creates a new UNIQUE constraint for the given columns
     * @param columns The names of the columns this constraint applies to
     * @return A new table constraint
     */
    public static TableConstraint<List<String>> UNIQUE(Collection<String> columns) {
        assert !columns.isEmpty();
        return new TableConstraint<>(Type.UNIQUE, List.copyOf(columns));
    }

    /**
     * Creates a new UNIQUE constraint for the given columns
     * @param columns The names of the columns this constraint applies to
     * @return A new table constraint
     */
    public static TableConstraint<List<String>> UNIQUE(String... columns) {
        assert columns.length > 0;
        return new TableConstraint<>(Type.UNIQUE, List.of(columns));
    }

    /**
     * Creates a new PRIMARY KEY constraint for the given columns
     * @param columns The names of the columns this constraint applies to
     * @return A new table constraint
     */
    public static TableConstraint<List<String>> PRIMARY_KEY(Collection<String> columns) {
        assert !columns.isEmpty();
        return new TableConstraint<>(Type.PRIMARY_KEY, List.copyOf(columns));
    }

    /**
     * Creates a new PRIMARY KEY constraint for the given columns
     * @param columns The names of the columns this constraint applies to
     * @return A new table constraint
     */
    public static TableConstraint<List<String>> PRIMARY_KEY(String... columns) {
        assert columns.length > 0;
        return new TableConstraint<>(Type.PRIMARY_KEY, List.of(columns));
    }

    /**
     * Creates a new FOREIGN KEY constraint for the given column
     * @param column The name of the column this constraint applies to
     * @param ref A reference to the column referenced by this foreign key
     * @return A new table constraint
     * @see ReferenceConstraint
     */
    public static ReferenceConstraint FOREIGN_KEY(String column, ColumnRef ref) {
        SQLUtil.validate(column);
        return new ReferenceConstraint(column, ref);
    }

    /**
     * Crates a CHECK constraint using the given expression
     * @param condition An expression to evaluate each time the table is updated
     * @return A new table constraint
     */
    public static TableConstraint<Expression> CHECK(Expression condition) {
        return new TableConstraint<>(Type.CHECK, condition);
    }

    /**
     * Represents a foreign key constraint
     */
    public static class ReferenceConstraint extends TableConstraint<ColumnRef> {

        public final String column;
        public ReferenceAction deleteAction = null;
        public ReferenceAction updateAction = null;

        /**
         * Creates a new FOREIGN KEY constraint for the given column
         * @param column The name of the column this constraint applies to
         * @param ref A reference to the column referenced by this foreign key
         */
        public ReferenceConstraint(String column, ColumnRef ref) {
            super(Type.FOREIGN_KEY, ref);
            this.column = column;
        }

        /**
         * Sets the delete action of this reference constraint to cascade
         * @return A reference to self
         * @deprecated use onDelete instead
         */
        @Deprecated
        public ReferenceConstraint cascade() {
            this.deleteAction = ReferenceAction.CASCADE;
            return this;
        }

        /**
         * Sets the delete action of this reference constraint
         * @param action The action to take when the referenced column is deleted
         * @return A reference to self
         */
        public ReferenceConstraint onDelete(ReferenceAction action) {
            this.deleteAction = action;
            return this;
        }

        /**
         * Sets the update action of this reference constraint
         * @param action The action to take when the referenced column is updated
         * @return A reference to self
         */
        public ReferenceConstraint onUpdate(ReferenceAction action) {
            this.updateAction = action;
            return this;
        }
    }

    /**
     * An action taken when a column referenced by a foreign key constraint is updated or deleted
     */
    public enum ReferenceAction {
        RESTRICT("RESTRICT"),
        CASCADE("CASCADE"),
        NO_ACTION("NO ACTION"),
        SET_DEFAULT("SET DEFAULT"),
        SET_NULL("SET NULL");

        public final String grammar;
        ReferenceAction(String grammar) {
            this.grammar = grammar;
        }
    }

    /**
     * A type of table constraint
     */
    public enum Type {
        UNIQUE,
        CHECK,
        PRIMARY_KEY,
        FOREIGN_KEY
    }


}
