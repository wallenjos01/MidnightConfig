package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Condition {

    public final Operand operand;
    public final String key;

    private boolean inverted;
    private final List<DataValue<?>> arguments = new ArrayList<>();
    public final List<Tuples.T2<Conjunction, Condition>> children = new ArrayList<>();

    private Condition(String key, Operand operand) {
        this.operand = operand;
        this.key = key;
    }

    public Condition and(Condition other) {
        children.add(new Tuples.T2<>(Conjunction.AND, other));
        return this;
    }

    public Condition or(Condition other) {
        children.add(new Tuples.T2<>(Conjunction.OR, other));
        return this;
    }

    public DataValue<?> getArgument(int value) {
        return arguments.get(value);
    }

    public void writeArguments(PreparedStatement stmt, int startIndex) throws SQLException {

        for(DataValue<?> dv : arguments) {
            dv.write(stmt, startIndex++);
        }
        for(Tuples.T2<Conjunction, Condition> t : children) {
            t.p2.writeArguments(stmt, startIndex);
        }
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public boolean isInverted() {
        return inverted;
    }

    public Condition invert() {
        inverted = true;
        return this;
    }

    private Condition arg(DataValue<?> value) {
        this.arguments.add(value);
        return this;
    }


    public static Condition equals(String key, DataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Condition(key, Operand.EQUALS).arg(value);
    }

    public static Condition greaterThan(String key, DataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Condition(key, Operand.GREATER_THAN).arg(value);
    }

    public static Condition lessThan(String key, DataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Condition(key, Operand.LESS_THAN).arg(value);
    }

    public static Condition atLeast(String key, DataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Condition(key, Operand.AT_LEAST).arg(value);
    }

    public static Condition atMost(String key, DataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Condition(key, Operand.AT_MOST).arg(value);
    }

    public static <T extends DataValue<?>> Condition between(String key, T min, T max) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Condition(key, Operand.BETWEEN).arg(min).arg(max);
    }

    public static Condition in(String key, Stream<DataValue<?>> values) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        Condition out = new Condition(key, Operand.IN);
        values.forEach(out::arg);
        return out;
    }

    public enum Conjunction {
        AND,
        OR
    }

    public enum Operand {
        EQUALS,
        LESS_THAN,
        GREATER_THAN,
        AT_LEAST,
        AT_MOST,
        BETWEEN,
        IN
    }

    public String encode() {
        StringBuilder builder = new StringBuilder();
        if(inverted) {
            builder.append("NOT ");
        }
        builder.append(key);

        switch (operand) {
            case EQUALS: builder.append(" = ?"); break;
            case GREATER_THAN: builder.append(" > ?"); break;
            case LESS_THAN: builder.append(" < ?"); break;
            case AT_LEAST: builder.append(" >= ?"); break;
            case AT_MOST: builder.append(" <= ?"); break;
            case BETWEEN: builder.append(" BETWEEN ? AND ?"); break;
            case IN: {
                builder.append(" IN(");
                for(int i = 0; i < getArgumentCount() ; i++) {
                    if(i > 0) {
                        builder.append(',');
                    }
                    builder.append('?');
                }
                builder.append(')');
                break;
            }
        }

        for(Tuples.T2<Condition.Conjunction, Condition> child : children) {
            if(child.p1 == Condition.Conjunction.AND) {
                builder.append(" AND (");
            } else {
                builder.append(" OR (");
            }
            builder.append(child.p2.encode());
            builder.append(")");
        }

        return builder.toString();
    }

}
