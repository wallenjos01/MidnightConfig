package org.wallentines.mdcfg.serializer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContextMap {

    private final List<Object> values;
    private final Map<Class<?>, List<Object>> valuesByClass;

    public static final ContextMap EMPTY = new ContextMap();

    public ContextMap() {
        this.values = Collections.emptyList();
        this.valuesByClass = Collections.emptyMap();
    }

    public ContextMap(List<Object> values) {

        List<Object> inValues = new ArrayList<>();
        Map<Class<?>, List<Object>> inValuesByClass = new HashMap<>();
        for(Object value : values) {
            inValues.add(value);
            addWithSuperclasses(inValuesByClass, value.getClass(), value);
        }

        this.values = List.copyOf(inValues);
        this.valuesByClass = Map.copyOf(inValuesByClass);
    }

    private ContextMap(List<Object> values, Map<Class<?>, List<Object>> valuesByClass) {
        this.values = values;
        this.valuesByClass = valuesByClass;
    }

    public List<Object> values() {
        return values;
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<T> getByClass(Class<T> clazz) {
        if(clazz == Object.class) return (Stream<T>) values().stream();

        List<Object> values = valuesByClass.get(clazz);
        if (values == null) {
            return Stream.empty();
        }
        return (Stream<T>) values.stream();
    }

    public <T> Optional<T> getFirst(Class<T> clazz) {
        return getByClass(clazz).findFirst();
    }

    public ContextMap and(ContextMap other) {
        if(other == EMPTY) return this;
        if(this == EMPTY) return other;
        if(this == other) return this;

        Map<Class<?>, List<Object>> valuesByClass = new HashMap<>(this.valuesByClass);
        joinClassCache(valuesByClass, other);

        return new ContextMap(
                Stream.concat(values.stream(), other.values.stream()).collect(Collectors.toList()),
                valuesByClass);
    }

    public ContextMap and(Stream<ContextMap> other) {
        List<ContextMap> contexts = other.collect(Collectors.toList());

        Map<Class<?>, List<Object>> valuesByClass = new HashMap<>(this.valuesByClass);
        for (ContextMap context : contexts) {
            joinClassCache(valuesByClass, context);
        }

        return new ContextMap(
                Stream.concat(values.stream(), contexts.stream().flatMap(c -> c.values().stream())).collect(Collectors.toList()));
    }

    private void joinClassCache(Map<Class<?>, List<Object>> valuesByClass, ContextMap context) {
        for(Class<?> c : context.valuesByClass.keySet()) {
            valuesByClass.compute(c, (k,v) -> {
                if(v == null) v = new ArrayList<>();
                v.addAll(context.valuesByClass.get(c));
                return v;
            });
        }
    }


    public static ContextMap of(Object... values) {
        if(values == null || values.length == 0) return EMPTY;
        return new ContextMap(Arrays.stream(values).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Object... values) {
        Builder out = new Builder();
        if(values != null && values.length > 0) {
            out.addAll(Arrays.stream(values).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return out;
    }

    public static class Builder {

        private final List<Object> values = new ArrayList<>();

        public Builder add(Object... values) {
            if(values.length == 1) {
                this.values.add(values[0]);
            } else {
                this.values.addAll(Arrays.asList(values));
            }
            return this;
        }

        public Builder addAll(List<Object> values) {
            if(values != null && !values.isEmpty()) {
                this.values.addAll(values);
            }
            return this;
        }

        public ContextMap build() {
            return new ContextMap(values);
        }

    }

    private void addWithSuperclasses(Map<Class<?>, List<Object>> map, Class<?> clazz, Object value) {

        if(clazz == Object.class) return;
        map.computeIfAbsent(clazz, k -> new ArrayList<>()).add(value);

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            addWithSuperclasses(map, superclass, value);
        }
        for(Class<?> interfaceClass : clazz.getInterfaces()) {
            addWithSuperclasses(map, interfaceClass, value);
        }
    }
}
