package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.Expression;
import org.wallentines.mdcfg.sql.stmt.StatementBuilder;
import org.wallentines.mdcfg.sql.stmt.Term;
public class Condition implements Expression {

    public final Operand operand;

    public final Term lhs;
    private final Term rhs;

    private Condition(Term lhs, Operand operand, Term rhs) {
        this.lhs = lhs;
        this.operand = operand;
        this.rhs = rhs;
    }

    public static Condition equals(String key, Term value) {
        return equals(Term.literal(key), value);
    }
    public static Condition equals(Term key, Term value) {
        return new Condition(key, Operand.EQUALS, value);
    }

    public static Condition greaterThan(String key, Term value) {
        return greaterThan(Term.literal(key), value);
    }
    public static Condition greaterThan(Term key, Term value) {
        return new Condition(key, Operand.GREATER_THAN, value);
    }

    public static Condition lessThan(String key, Term value) {
        return lessThan(Term.literal(key), value);
    }
    public static Condition lessThan(Term key, Term value) {
        return new Condition(key, Operand.LESS_THAN, value);
    }

    public static Condition atLeast(String key, Term value) {
        return atLeast(Term.literal(key), value);
    }
    public static Condition atLeast(Term key, Term value) {
        return new Condition(key, Operand.AT_LEAST, value);
    }

    public static Condition atMost(String key, Term value) {
        return atMost(Term.literal(key), value);
    }
    public static Condition atMost(Term key, Term value) {
        return new Condition(key, Operand.AT_MOST, value);
    }

    @Override
    public void write(StatementBuilder builder) {
        encode(builder);
    }

    public enum Operand {
        EQUALS,
        LESS_THAN,
        GREATER_THAN,
        AT_LEAST,
        AT_MOST
    }

    public void encode(StatementBuilder builder) {
        builder.appendTerm(lhs);

        switch (operand) {
            case EQUALS: builder.append(" = ").appendTerm(rhs); break;
            case GREATER_THAN: builder.append(" > ").appendTerm(rhs); break;
            case LESS_THAN: builder.append(" < ").appendTerm(rhs); break;
            case AT_LEAST: builder.append(" >= ").appendTerm(rhs); break;
            case AT_MOST: builder.append(" <= ").appendTerm(rhs); break;

        }
    }

}
