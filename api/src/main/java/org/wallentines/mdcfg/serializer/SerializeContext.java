package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.ConfigPrimitive;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    SerializeResult<String> asString(T object);

    /**
     * Interprets the given encode-able object as a Number
     * @param object The object to read
     * @return A Number, or null if the object cannot be interpreted as such
     */
    SerializeResult<Number> asNumber(T object);

    /**
     * Interprets the given encode-able object as a Boolean
     * @param object The object to read
     * @return A Boolean, or null if the object cannot be interpreted as such
     */
    SerializeResult<Boolean> asBoolean(T object);

    /**
     * Interprets the given encode-able object as a blob
     * @param object The object to read
     * @return A Blob, or null if the object cannot be interpreted as such
     */
    SerializeResult<ByteBuffer> asBlob(T object);

    /**
     * Interprets the given encode-able object as a List
     * @param object The object to read
     * @return A List, or null if the object cannot be interpreted as such
     */
    SerializeResult<Collection<T>> asList(T object);

    /**
     * Interprets the given encode-able object as a Map
     * @param object The object to read
     * @return A Map, or null if the object cannot be interpreted as such
     */
    SerializeResult<Map<String, T>> asMap(T object);

    /**
     * Interprets the given encode-able object as a Map which retains its key order
     * @param object The object to read
     * @return A Map, or null if the object cannot be interpreted as such
     */
    SerializeResult<Map<String, T>> asOrderedMap(T object);

    /**
     * Determines if the given encode-able object should be treated as null
     * @param object the object to inspect
     * @return Whether the object is null in the current context
     */
    default boolean isNull(T object) {
        return getType(object) == Type.NULL;
    }

    /**
     * Determines if the given encode-able object can be interpreted as a String
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a String
     */
    default boolean isString(T object) {
        return getType(object) == Type.STRING;
    }

    /**
     * Determines if the given encode-able object can be interpreted as a Number
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Number
     */
    default boolean isNumber(T object) {
        return getType(object) == Type.NUMBER;
    }

    /**
     * Determines if the given encode-able object can be interpreted as a Boolean
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Boolean
     */
    default boolean isBoolean(T object) {
        return getType(object) == Type.BOOLEAN;
    }

    /**
     * Determines if the given encode-able object can be interpreted as a Map
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Map
     */
    default boolean isBlob(T object) {
        return getType(object) == Type.BLOB;
    }

    /**
     * Determines if the given encode-able object can be interpreted as a List
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a List
     */
    default boolean isList(T object) {
        return getType(object) == Type.LIST;
    }

    /**
     * Determines if the given encode-able object can be interpreted as a Map
     * @param object The object to inspect
     * @return Whether the object can be interpreted as a Map
     */
    default boolean isMap(T object) {
        return getType(object) == Type.MAP;
    }

    /**
     * Determines the basic type of the given encode-able object
     * @param object The object to inspect
     * @return The object's basic type.
     */
    Type getType(T object);

    /**
     * Determines if the two given encode-able object are of the same type
     * @param object The object to inspect
     * @param other The object to compare to
     * @return Whether the two objects are the same type
     */
    default boolean isSimilar(T object, T other) {
        return getType(object) == getType(other);
    }

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
     * Converts the given ByteBuffer into an encode-able object
     * @param object The ByteBuffer to convert
     * @return An encode-able object
     */
    T toBlob(ByteBuffer object);

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
     * Creates a null value according to the current context
     * @return A null value
     */
    T nullValue();

    /**
     * Merges the given list of encode-able objects into the given object, assuming it is a list
     * @param list The list to merge from
     * @param object The object to merge into
     * @return A new list, merged from the given data, or the original object if the object was not a list
     */
    default T mergeList(Collection<T> list, T object) {
        SerializeResult<Collection<T>> objs = asList(object);
        if(!objs.isComplete()) return object;

        Collection<T> out = objs.getOrNull();
        out.addAll(list);

        return toList(out);
    }

    /**
     * Merges the given encode-able objects together, if both are actually maps
     * @param value The map read from
     * @param other The map merge into
     * @return A new map consisting of entries from both maps, or a copy of the second object
     */
    default T mergeMap(T value, T other) {

        if(!isMap(value) || !isMap(other)) return copy(other);
        for(String key : getOrderedKeys(other)) {
            if(isNull(get(key, value))) {
                set(key, copy(get(key, other)), value);
            }
        }
        return value;
    }

    /**
     * Merges the given encode-able objects together, if both are actually maps. Values in the first map will overwrite
     * values with the same key in the second map
     * @param value The map read from
     * @param other The map merge into
     * @return A new map consisting of entries from both maps, or a copy of the second object
     */
    default T mergeMapOverwrite(T value, T other) {
        if(!isMap(value) || !isMap(other)) return copy(other);
        for(String key : getOrderedKeys(other)) {
            set(key, copy(get(key, other)), value);
        }
        return value;
    }

    /**
     * Merges two encode-able objects together, if possible
     * @param value The value to read from
     * @param other The value to merge into
     * @return A merged value, if possible, or a copy of the second value if not
     */
    default T merge(T value, T other) {
        if(isNull(other)) {
            return nullValue();
        }
        if(isMap(other)) {
            if(!isMap(value)) return copy(other);
            return mergeMap(value, other);
        }
        if(isSimilar(value, other)) {
            return value;
        }

        return copy(other);
    }

    /**
     * Sets the value associated with the given key on the given object
     * @param key The key to save the value as
     * @param value The value to save
     * @param object The object put values into
     * @return A map with the value set, or a null value if the given object was not a map
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

        if(other.getClass() == getClass()) return (O) object;

        switch (getType(object)) {
            case STRING:
                return other.toString(asString(object).getOrThrow());
            case NUMBER:
                return other.toNumber(asNumber(object).getOrThrow());
            case BOOLEAN:
                return other.toBoolean(asBoolean(object).getOrThrow());
            case BLOB:
                return other.toBlob(asBlob(object).getOrThrow());
            case LIST:
                return other.toList(asList(object).getOrThrow().stream()
                        .map(t -> convert(other, t)).collect(Collectors.toList()));
            case MAP: {
                O out = other.toMap(new LinkedHashMap<>());
                for(String key : getOrderedKeys(object)) {
                    T value = get(key, object);
                    other.set(key, convert(other, value), out);
                }
                return out;
            }
            case NULL:
                return other.nullValue();
            default:
                throw new SerializeException("Don't know how to convert " + object + " to another context!");
        }
    }

    /**
     * Clones the given object
     * @param object The object to clone
     * @return A cloned object
     */
    default T copy(T object) {

        switch (getType(object)) {
            case STRING:
                return toString(asString(object).getOrThrow());
            case NUMBER:
                return toNumber(copyNumber(asNumber(object).getOrThrow()));
            case BOOLEAN:
                return toBoolean(asBoolean(object).getOrThrow());
            case BLOB:
                return toBlob(asBlob(object).getOrThrow());
            case LIST: {
                List<T> out = new ArrayList<>();
                for (T value : asList(object).getOrThrow()) {
                    out.add(copy(value));
                }
                return toList(out);
            }
            case MAP: {
                T out = toMap(new LinkedHashMap<>());
                for (String key : getOrderedKeys(object)) {
                    T value = get(key, object);
                    set(key, copy(value), out);
                }
                return out;
            }
            case NULL:
                return nullValue();
            default:
                throw new SerializeException("Don't know how to clone " + object + "in this context!");

        }
    }

    /**
     * Whether the given object supports storing additional key-value metadata.
     * Note: this metadata is not guaranteed to be preserved when writing to disk!
     * @param object The object to inspect
     * @return Whether that object supports metadata
     */
    boolean supportsMeta(T object);

    /**
     * Gets a metadata value on the given object associated with the given key
     * @param object The object to lookup on
     * @param key The metadata key
     * @return A metadata value, or null
     */
    String getMetaProperty(T object, String key);

    /**
     * Sets a metadata value on the given object associated with the given key
     * @param object The object to modify
     * @param key The metadata key
     * @param value A metadata value
     */
    void setMetaProperty(T object, String key, String value);


    /**
     * Gets the first context value with the given type
     * @param type The type to lookup
     * @return An optional which may contain a value of the given type
     * @param <C> The type to lookup
     */
    default <C> Optional<C> getFirst(Class<C> type) {
        return Optional.empty();
    }

    /**
     * Gets a stream of context values which conform to the given type
     * @param type The type to lookup
     * @return A stream of values with that type
     * @param <C> The type to lookup
     */
    default <C> Stream<C> getByClass(Class<C> type) {
        return Stream.empty();
    }

    /**
     * Creates a new SerializeContext which has the given context value
     * @param value The value
     * @return A SerializeContext with the given context value
     * @param <C> The type of value
     */
    default <C> SerializeContext<T> withContextValue(C value) {
        return new DelegatedContext<T, Void>(this, null, ContextMap.of(value));
    }

    /**
     * Creates a new SerializeContext which has the given context map
     * @param contextMap The ContextMap to add
     * @return A SerializeContext with the given context map
     * @param <C> The type of value
     */
    default <C> SerializeContext<T> withContextMap(ContextMap contextMap) {
        return new DelegatedContext<T, Void>(this, null, contextMap);
    }

    /**
     * Copies a number value
     * @param number The number to copy
     * @return A copy of that number
     */
    static Number copyNumber(Number number) {
        if(number instanceof Integer) return number.intValue();
        if(number instanceof Long) return number.longValue();
        if(number instanceof Float) return number.floatValue();
        if(number instanceof Short) return number.shortValue();
        if(number instanceof Double) return number.doubleValue();
        if(number instanceof Byte) return number.byteValue();

        if(ConfigPrimitive.isInteger(number)) {
            return number.longValue();
        }
        return number.doubleValue();
    }


    enum Type {
        UNKNOWN,
        NULL,
        STRING,
        NUMBER,
        BOOLEAN,
        BLOB,
        LIST,
        MAP
    }
}
