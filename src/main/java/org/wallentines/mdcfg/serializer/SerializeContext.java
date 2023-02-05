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

    T mergeList(Collection<T> list, T object);

    T fill(T value, T other);
    T fillOverwrite(T value, T other);

    T set(String key, T value, T object);

    default <O> O convert(SerializeContext<O> other, T object) {

        if(object == null) return null;

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
