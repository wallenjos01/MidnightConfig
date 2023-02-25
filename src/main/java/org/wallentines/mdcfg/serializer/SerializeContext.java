package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Tuples;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public interface SerializeContext<T> {

    String asString(T object);
    Number asNumber(T object);
    Boolean asBoolean(T object);
    Collection<T> asList(T object);
    Map<String, T> asMap(T object);
    Map<String, T> asOrderedMap(T object);

    boolean isString(T object);
    boolean isNumber(T object);
    boolean isBoolean(T object);
    boolean isList(T object);
    boolean isMap(T object);

    Collection<String> getOrderedKeys(T object);
    T get(String key, T object);

    T toString(String object);
    T toNumber(Number object);
    T toBoolean(Boolean object);
    T toList(Collection<T> list);
    T toMap(Map<String, T> map);

    default T mergeList(Collection<T> list, T object) {
        if(!isList(object)) return null;
        Collection<T> objs = asList(object);
        if(list != null) objs.addAll(list);
        return toList(objs);
    }

    default T mergeMap(T value, T other) {
        if(!isMap(value) || !isMap(other)) return null;
        for(String key : getOrderedKeys(other)) {
            if(get(key, value) == null) {
                set(key, get(key, other), value);
            }
        }
        return value;
    }
    default T mergeMapOverwrite(T value, T other) {
        if(!isMap(value) || !isMap(other)) return null;
        for(String key : getOrderedKeys(other)) {
            set(key, get(key, other), value);
        }
        return value;
    }
    default T merge(T value, T other) {

        if(isMap(other)) {
            if(!isMap(value)) return other;
            return mergeMap(value, other);
        }
        if(isList(value) && isList(other) ||
            isString(value) && isString(other) ||
            isNumber(value) && isNumber(other) ||
            isBoolean(value) && isBoolean(other)) {
            return value;
        }

        return other;
    }

    T set(String key, T value, T object);

    @SuppressWarnings("unchecked")
    default <O> O convert(SerializeContext<O> other, T object) {

        if(object == null) return null;
        if(other == this) return (O) object;

        if(isString(object)) {
            return other.toString(asString(object));
        }
        if(isNumber(object)) {
            return other.toNumber(asNumber(object));
        }
        if(isBoolean(object)) {
            return other.toBoolean(asBoolean(object));
        }
        if(isList(object)) {
            return other.toList(asList(object).stream()
                    .map(t -> convert(other, t)).collect(Collectors.toList()));
        }
        if(isMap(object)) {
            return other.toMap(asMap(object).entrySet().stream()
                    .map(ent -> new Tuples.T2<>(ent.getKey(), convert(other, ent.getValue())))
                    .filter(t2 -> t2.p1 != null && t2.p2 != null)
                    .collect(Collectors.toMap(t2 -> t2.p1, t2 -> t2.p2)));
        }

        throw new SerializeException("Don't know how to convert " + object + " to another context!");
    }

}
