package org.wallentines.mdcfg.sql.stmt;

import java.util.stream.Stream;

public interface Expression extends Term {

    default Expression invert() {
        return new Inverted(this);
    }

    default Expression and(Expression other) {
        return new Composite(this, other, Conjunction.AND);
    }

    default Expression or(Expression other) {
        return new Composite(this, other, Conjunction.OR);
    }

    static Exists exists(DQLStatement statement) {
        return new Exists(statement);
    }

    static Unique unique(DQLStatement statement) {
        return new Unique(statement);
    }

    static Between between(String lhs, Term lower, Term higher) {
        return between(Term.literal(lhs), lower, higher);
    }

    static Between between(Term lhs, Term lower, Term higher) {
        return new Between(lhs, lower, higher);
    }

    static In in(Term lhs, Term rhs) {
        return new In(lhs, rhs);
    }

    static In in(Term lhs, Stream<Term> rhs) {
        return new In(lhs, Term.array(rhs));
    }

    static In in(String lhs, Term rhs) {
        return new In(Term.literal(lhs), rhs);
    }

    static In in(String lhs, Stream<Term> rhs) {
        return new In(Term.literal(lhs), Term.array(rhs));
    }

    static Null isNull(String lhs) {
        return new Null(Term.literal(lhs));
    }

    static Null isNull(Term lhs) {
        return new Null(lhs);
    }

    static Null isNotNull(String lhs) {
        return new Null(Term.literal(lhs), true);
    }

    static Null isNotNull(Term lhs) {
        return new Null(lhs, true);
    }

    class Inverted implements Expression {
        private final Expression inner;
        public Inverted(Expression inner) {
            this.inner = inner;
        }

        @Override
        public Expression invert() {
            return inner;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.append("NOT (").appendExpression(inner).append(")");
        }
    }

    class Composite implements Expression {
        private final Expression base;
        private final Expression next;
        private final Conjunction conjunction;

        public Composite(Expression base, Expression next, Conjunction conjunction) {
            this.base = base;
            this.next = next;
            this.conjunction = conjunction;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.appendExpression(base);
            builder.append(conjunction == Conjunction.AND ? " AND " : " OR ");
            builder.append("(").appendExpression(next).append(")");
        }
    }

    class Between implements Expression {

        private final Term lhs;
        private final Term lower;
        private final Term upper;

        public Between(Term lhs, Term lower, Term upper) {
            this.lhs = lhs;
            this.lower = lower;
            this.upper = upper;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.appendTerm(lhs).append(" BETWEEN ").appendTerm(lower).append(" AND ").appendTerm(upper);
        }
    }


    class In implements Expression {

        private final Term lhs;
        private final Term rhs;

        public In(Term lhs, Term rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.appendTerm(lhs).append(" IN (").appendTerm(rhs).append(")");
        }
    }

    class Null implements Expression {

        private final Term lhs;
        private boolean not;

        public Null(Term lhs) {
            this.lhs = lhs;
        }

        public Null(Term lhs, boolean not) {
            this.lhs = lhs;
            this.not = not;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.appendTerm(lhs).append(" IS");
            if (not) { builder.append(" NOT"); }
            builder.append(" NULL");
        }
    }

    class Exists implements Expression {
        private final DQLStatement statement;

        public Exists(DQLStatement statement) {
            this.statement = statement;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.append("EXISTS (").append(statement.toBuilder()).append(")");
        }
    }

    class Unique implements Expression {
        private final DQLStatement statement;
        private Distinct nullsDistinct;

        public Unique(DQLStatement statement) {
            this.statement = statement;
        }

        public Unique nullsDistinct(Distinct distinct) {
            this.nullsDistinct = distinct;
            return this;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.append("UNIQUE");
            if(nullsDistinct != null) {
                builder.append(" NULLS ");
                if(nullsDistinct == Distinct.ALL) builder.append("ALL ");
                else if(nullsDistinct == Distinct.NONE) builder.append("NOT ");
                builder.append("DISTINCT");
            }
            builder.append(" (").append(statement.toBuilder()).append(")");
        }
    }

    enum Distinct {
        ONE,
        ALL,
        NONE
    }

    enum Conjunction {
        AND,
        OR
    }


}
