package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigPrimitive;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Where {

    private final StringBuilder current;

    private Where(StringBuilder initial) {
        this.current = initial;
    }

    public Where and(Where other) {
        current.append("AND (").append(other.current.toString()).append(")");
        return this;
    }

    public Where or(Where other) {
        current.append("OR (").append(other.current.toString()).append(")");
        return this;
    }

    public String toString() {
        return "WHERE " + current.toString();
    }


    public static Where equals(String key, ConfigPrimitive value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" = ").append(SQLUtil.encodePrimitive(value)));
    }

    public static Where greaterThan(String key, Number value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" > ").append(value));
    }

    public static Where lessThan(String key, Number value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" < ").append(value));
    }

    public static Where atLeast(String key, Number value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" >= ").append(value));
    }

    public static Where atMost(String key, Number value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" <= ").append(value));
    }

    public static Where not(String key, Number value) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" <= ").append(value));
    }

    public static <T extends Number> Where between(String key, T min, T max) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" BETWEEN ").append(min).append(" AND ").append(max));
    }

    public static Where in(String key, Stream<ConfigPrimitive> values) {
        if(!SQLUtil.VALID_NAME.matcher(key).matches()) throw new IllegalArgumentException("Invalid column name: " + key);
        return new Where(new StringBuilder(key).append(" IN (").append(values.map(SQLUtil::encodePrimitive).collect(Collectors.joining(","))).append(")"));
    }

}
