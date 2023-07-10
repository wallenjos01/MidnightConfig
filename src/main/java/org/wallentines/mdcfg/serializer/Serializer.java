package org.wallentines.mdcfg.serializer;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface Serializer<T> {

    /**
     * Serializes an object with type T into an object of type O, using the given context
     * @param context The context by which to convert the given value to another type
     * @param value The value to serialize
     * @return A SerializeResult containing the serialized object, or an error String if serialization failed
     * @param <O> The type to serialize the value into
     */
    <O> SerializeResult<O> serialize(SerializeContext<O> context, T value);

    /**
     * Deserializes an object from type O into an object of type T, using the given context
     * @param context The context by which to convert the given value to another type
     * @param value The value to deserialize
     * @return A SerializeResult containing the deserialized object, or an error String if deserialization failed
     * @param <O> The type to deserialize the value from
     */
    <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value);

    /**
     * Creates an entry for use in an ObjectSerializer using the given key and getter
     * @param key The key to use for lookups and saving
     * @param getter A function to retrieve an instance of T from an instance of O when serializing
     * @return An entry for use in ObjectSerializer.create()
     * @param <O> The type of object to serialize
     */
    default <O> ObjectSerializer.Entry<T, O> entry(String key, Function<O, T> getter) {
        return ObjectSerializer.entry(key, this, getter);
    }

    /**
     * Creates a list serializer from this serializer
     * @return A serializer for a list of objects with type T
     */
    default ListSerializer<T> listOf() {
        return new ListSerializer<>(this);
    }

    /**
     * Creates a list serializer from this serializer which does not require all contained objects to (de-)serialize successfully
     * @return A serializer for a list of objects with type T
     */
    default ListSerializer<T> filteredListOf() {
        return new ListSerializer<>(this, (err) -> {});
    }

    /**
     * Creates a list serializer from this serializer which does not require all contained objects to (de-)serialize successfully
     * @param onError A callback to send error text whenever an object fails to serialize
     * @return A serializer for a list of objects with type T
     */
    default ListSerializer<T> filteredListOf(Consumer<String> onError) {
        return new ListSerializer<>(this, onError);
    }


    /**
     * Creates a map serializer from this serializer
     * @return A serializer for a map with string keys and T values
     */
    default MapSerializer<String, T> mapOf() {
        return mapOf(InlineSerializer.RAW);
    }

    /**
     * Creates a map serializer from this serializer using the given key serializer
     * @param keySerializer The serializer to use to serialize the keys in the map to Strings
     * @return A serializer for map with K keys and T values
     * @param <K> The type of values for the keys in the map
     */
    default <K> MapSerializer<K, T> mapOf(InlineSerializer<K> keySerializer) {
        return new MapSerializer<>(keySerializer, this);
    }

    /**
     * Creates a map serializer from this serializer which does not require all contained objects to (de-)serialize successfully
     * @return A serializer for a map with string keys and T values
     */
    default MapSerializer<String, T> filteredMapOf() {
        return filteredMapOf(InlineSerializer.RAW);
    }

    /**
     * Creates a map serializer from this serializer using the given key serializer which does not require all contained objects to (de-)serialize successfully
     * @param keySerializer The serializer to use to serialize the keys in the map to Strings
     * @return A serializer for map with K keys and T values
     * @param <K> The type of values for the keys in the map
     */
    default <K> MapSerializer<K, T> filteredMapOf(InlineSerializer<K> keySerializer) {
        return new MapSerializer<>(keySerializer, this, true);
    }

    /**
     * Creates a new serializer with a fallback serializer. If serialization fails, the fallback will be used instead
     * @param other The fallback serializer
     * @return A new serializer with the given fallback.
     */
    default Serializer<T> or(Serializer<T> other) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return Serializer.this.serialize(context, value).mapError(() -> other.serialize(context, value));
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return Serializer.this.deserialize(context, value).mapError(() -> other.deserialize(context, value));
            }
        };
    }

    default <O> Serializer<O> map(Function<O, T> getter, Function<T, O> construct) {
        return new Serializer<>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                return Serializer.this.serialize(context, getter.apply(value));
            }

            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                return Serializer.this.deserialize(context, value).flatMap(construct);
            }
        };
    }

    // Default Serializers
    Serializer<String> STRING = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, String value) {
            return SerializeResult.ofNullable(context.toString(value), "Unable to serialize " + value + " as a String!");
        }
        @Override
        public <O> SerializeResult<String> deserialize(SerializeContext<O> context, O value) {
            return SerializeResult.ofNullable(context.asString(value), "Unable to deserialize " + value + " as a String!");
        }
    };

    Serializer<Byte> BYTE = NumberSerializer.forByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
    Serializer<Short> SHORT = NumberSerializer.forShort(Short.MIN_VALUE, Short.MAX_VALUE);
    Serializer<Integer> INT = NumberSerializer.forInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    Serializer<Long> LONG = NumberSerializer.forLong(Long.MIN_VALUE, Long.MAX_VALUE);
    Serializer<Float> FLOAT = NumberSerializer.forFloat(-Float.MAX_VALUE, Float.MAX_VALUE);
    Serializer<Double> DOUBLE = NumberSerializer.forDouble(-Double.MAX_VALUE, Double.MAX_VALUE);
    Serializer<Boolean> BOOLEAN = BooleanSerializer.RAW.or(BooleanSerializer.NUMBER).or(BooleanSerializer.STRING);

    Serializer<UUID> UUID = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, java.util.UUID value) {
            return SerializeResult.ofNullable(value, "Unable to save " + value + " as a UUID!").map(uuid -> SerializeResult.ofNullable(context.toString(value.toString())));
        }

        @Override
        public <O> SerializeResult<java.util.UUID> deserialize(SerializeContext<O> context, O value) {
            return SerializeResult.ofNullable(context.asString(value)).map(str -> {
                try {
                    return SerializeResult.success(java.util.UUID.fromString(str));
                } catch (IllegalArgumentException ex) {
                    return SerializeResult.failure("Unable to parse " + str + " as a UUID!");
                }
            });
        }
    };

}
