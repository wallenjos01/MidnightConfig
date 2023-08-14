package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Tuples;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An interface which allows serializers to turn data into an encode-able format, and for codecs to encode data from
 * that format
 * @param <T> The type of data to (de)serialize or encode/decode
 */
@SuppressWarnings("unused")
public interface SerializeContext<T> {

    /**
     * Interprets the given encode-able object as a String
     * @param object The object to read
     * @return A String, or null if the object cannot be interpreted as such
     */
    String asString(T object);

    /**
     * Interprets the given encode-able object as a Number
     * @param object The object to read
     * @return A Number, or null if the object cannot be interpreted as such
     */
    Number asNumber(T object);

    /**
     * Interprets the given encode-able object as a Boolean
     * @param object The object to read
     * @return A Boolean, or null if the object cannot be interpreted as such
     */
    Boolean asBoolean(T object);

    /**
     * Interprets the given encode-able object as a List
     * @param object The object to read
     * @return A List, or null if the object cannot be interpreted as such
     */
    Collection<T> asList(T object);

    /**
     * Interprets the given encode-able object as a Map
     * @param object The object to read
     * @return A Map, or null if the object cannot be interpreted as such
     */
    Map<String, T> asMap(T object);

    /**
     * Interprets the given encode-able object as a Map which retains its key order
     * @param object The object to read
     * @return A Map, or null if the object cannot be interpreted as such
     */
    Map<String, T> asOrderedMap(T object);


    /**
     * Determines if the given encode-able object can be interpreted as a String
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a String
     */
    boolean isString(T object);

    /**
     * Determines if the given encode-able object can be interpreted as a Number
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Number
     */
    boolean isNumber(T object);

    /**
     * Determines if the given encode-able object can be interpreted as a Boolean
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Boolean
     */
    boolean isBoolean(T object);

    /**
     * Determines if the given encode-able object can be interpreted as a List
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a List
     */
    boolean isList(T object);

    /**
     * Determines if the given encode-able object can be interpreted as a Map
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Map
     */
    boolean isMap(T object);

    /**
     * Gets the ordered keys for the given encode-able object, if it is a map
     * @param object The object to read
     * @return The keys for the object, or null if it is not a map
     */
    Collection<String> getOrderedKeys(T object);

    /**
     * Gets the value on the object associated with the given key, if it is a map
     * @param key The key to lookup
     * @param object The object to interpret
     * @return The value associated with the key, or null if the object is not a map or is not present
     */
    T get(String key, T object);

    /**
     * Converts the given string into an encode-able object
     * @param object The string to convert
     * @return An encode-able object
     */
    T toString(String object);

    /**
     * Converts the given number into an encode-able object
     * @param object The number to convert
     * @return An encode-able object
     */
    T toNumber(Number object);

    /**
     * Converts the given boolean into an encode-able object
     * @param object The boolean to convert
     * @return An encode-able object
     */
    T toBoolean(Boolean object);

    /**
     * Converts the given list into an encode-able object
     * @param list The list to convert
     * @return An encode-able object
     */
    T toList(Collection<T> list);

    /**
     * Converts the given map into an encode-able object
     * @param map The map to convert
     * @return An encode-able object
     */
    T toMap(Map<String, T> map);

    /**
     * Merges the given list of encode-able objects into the given object, assuming it is a list
     * @param list The list to merge from
     * @param object The object to merge into
     * @return A new list, merged from the given data, or null if the object was not a list
     */
    default T mergeList(Collection<T> list, T object) {
        if(!isList(object)) return null;
        Collection<T> objs = asList(object);
        if(list != null) objs.addAll(list);
        return toList(objs);
    }

    /**
     * Merges the given encode-able objects together, if both are actually maps
     * @param value The map read from
     * @param other The map merge into
     * @return A new map consisting of entries from both maps
     */
    default T mergeMap(T value, T other) {
        if(!isMap(value) || !isMap(other)) return null;
        for(String key : getOrderedKeys(other)) {
            if(get(key, value) == null) {
                set(key, get(key, other), value);
            }
        }
        return value;
    }

    /**
     * Merges the given encode-able objects together, if both are actually maps. Values in the first map will overwrite
     * values with the same key in the second map
     * @param value The map read from
     * @param other The map merge into
     * @return A new map consisting of entries from both maps
     */
    default T mergeMapOverwrite(T value, T other) {
        if(!isMap(value) || !isMap(other)) return null;
        for(String key : getOrderedKeys(other)) {
            set(key, get(key, other), value);
        }
        return value;
    }

    /**
     * Merges two encode-able objects together, if possible
     * @param value The value to read from
     * @param other The value to merge into
     * @return A merged value, if possible, or the second value if not
     */
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

    /**
     * Sets the value associated with the given key on the given object
     * @param key The key to save the value as
     * @param value The value to save
     * @param object The object put values into
     * @return A map with the value set, or null if the given object was not a map
     */
    T set(String key, T value, T object);

    /**
     * Converts the given object into another type of encode-able object using the given serialize context
     * @param other The serialize context to use to convert
     * @param object The object to convert
     * @return A new encode-able object
     * @param <O> The type of data to convert into
     */
    @SuppressWarnings("unchecked")
    default <O> O convert(SerializeContext<O> other, T object) {

        if(object == null) return null;
        if(other.getClass() == getClass()) return (O) object;

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
            return other.toMap(asOrderedMap(object).entrySet().stream()
                    .map(ent -> new Tuples.T2<>(ent.getKey(), convert(other, ent.getValue())))
                    .filter(t2 -> t2.p1 != null && t2.p2 != null)
                    .collect(Collectors.toMap(
                            t2 -> t2.p1, // Key mapper
                            t2 -> t2.p2, // Value mapper
                            (v1,v2) -> { throw new IllegalStateException("Found duplicate keys for values " + v1 + " and " + v2 + "!"); }, // Duplicate handler
                            LinkedHashMap::new // Map factory
                    )));
        }

        throw new SerializeException("Don't know how to convert " + object + " to another context!");
    }

}
