package org.wallentines.mdcfg.sql;

import java.util.*;

public class Column {


    private final String name;
    private final ColumnType<?> type;
    private final Map<Constraint.Type, Constraint<?>> constraints;

    private Column(String name, ColumnType<?> type, Collection<Constraint<?>> constraints) {
        this.name = name;
        this.type = type;
        this.constraints = new LinkedHashMap<>();
        for(Constraint<?> con : constraints) {
            this.constraints.putIfAbsent(con.type, con);
        }
    }

    public String getName() {
        return name;
    }

    public ColumnType<?> getType() {
        return type;
    }

    public Collection<Constraint<?>> getConstraints() {
        return constraints.values();
    }

    public Constraint<?> getConstraint(Constraint.Type type) {
        return constraints.get(type);
    }

    public boolean hasConstraint(Constraint.Type type) {
        return constraints.containsKey(type);
    }

    public static Builder builder(String name, ColumnType<?> type) {
        return new Builder(name, type);
    }

    public static Builder builder(String name, DataType<?> type) {
        return new Builder(name, new ColumnType<>(type));
    }

    public static class Builder {

        private final String name;
        private final ColumnType<?> type;
        private final List<Constraint<?>> constraints;

        public Builder(String name, ColumnType<?> type) {
            this.name = name;
            this.type = type;
            this.constraints = new ArrayList<>();
        }

        public Builder withConstraint(Constraint<?> constraint) {
            this.constraints.add(constraint);
            return this;
        }

        public Builder withConstraint(Constraint.Type type) {
            this.constraints.add(new Constraint<Void>(type, null));
            return this;
        }

        public <T> Builder withConstraint(Constraint.Type type, T value) {
            this.constraints.add(new Constraint<>(type, value));
            return this;
        }


        public Column build() {
            return new Column(name, type, constraints);
        }
    }

}
