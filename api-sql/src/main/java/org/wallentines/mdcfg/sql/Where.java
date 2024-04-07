package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.Tuples;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Where {

    public final Operand operand;
    public final String key;

    private boolean inverted;
    private final List<SQLDataValue<?>> arguments = new ArrayList<>();
    public final List<Tuples.T2<Conjunction, Where>> children = new ArrayList<>();

    private Where(String key, Operand operand) {
        this.operand = operand;
        this.key = key;
    }

    public Where and(Where other) {
        children.add(new Tuples.T2<>(Conjunction.AND, other));
        return this;
    }

    public Where or(Where other) {
        children.add(new Tuples.T2<>(Conjunction.OR, other));
        return this;
    }

    public SQLDataValue<?> getArgument(int value) {
        return arguments.get(value);
    }

    public void writeArguments(PreparedStatement stmt, int startIndex) throws SQLException {

        for(SQLDataValue<?> dv : arguments) {
            dv.write(stmt, startIndex++);
        }
        for(Tuples.T2<Conjunction, Where> t : children) {
            t.p2.writeArguments(stmt, startIndex);
        }
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public boolean isInverted() {
        return inverted;
    }

    public Where invert() {
        inverted = true;
        return this;
    }

    private Where arg(SQLDataValue<?> value) {
        this.arguments.add(value);
        return this;
    }


    public static Where equals(String key, SQLDataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(key, Operand.EQUALS).arg(value);
    }

    public static Where greaterThan(String key, SQLDataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(key, Operand.GREATER_THAN).arg(value);
    }

    public static Where lessThan(String key, SQLDataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(key, Operand.LESS_THAN).arg(value);
    }

    public static Where atLeast(String key, SQLDataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(key, Operand.AT_LEAST).arg(value);
    }

    public static Where atMost(String key, SQLDataValue<?> value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(key, Operand.AT_MOST).arg(value);
    }

    public static <T extends SQLDataValue<?>> Where between(String key, T min, T max) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(key, Operand.BETWEEN).arg(min).arg(max);
    }

    public static Where in(String key, Stream<SQLDataValue<?>> values) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        Where out = new Where(key, Operand.IN);
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

}
