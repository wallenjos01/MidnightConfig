package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.*;
import org.wallentines.mdcfg.codec.Codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An interface for serializing data into an encode-able format
 * @param <T> The type of data to serialize
 */
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
     * Creates a context-aware entry for use in an ObjectSerializer using the given key and getter
     * @param key The key to use for lookups and saving
     * @param getter A function to retrieve an instance of T from an instance of O when serializing
     * @return An entry for use in ObjectSerializer.create()
     * @param <O> The type of object to serialize
     */
    default <O,C> ContextObjectSerializer.ContextEntry<T, O, C> entry(String key, Functions.F2<O, C, T> getter) {
        return ContextObjectSerializer.entry(key, this, getter);
    }

    /**
     * Creates a context-aware entry for use in an ObjectSerializer using the given key and getter
     * @param key The key to use for lookups and saving
     * @param getter A function to retrieve an instance of T from an instance of O when serializing
     * @return An entry for use in ObjectSerializer.create()
     * @param <O> The type of object to serialize
     */
    default <O,C> ContextObjectSerializer.ContextEntry<T, O, C> entry(Functions.F1<C, String> key, Functions.F2<O, C, T> getter) {
        return ContextObjectSerializer.entry(key, this, getter);
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
        return new ListSerializer<>(this, (err) -> false);
    }

    /**
     * Creates a list serializer from this serializer which does not require all contained objects to (de-)serialize successfully
     * @param onError A callback to send error text whenever an object fails to serialize
     * @return A serializer for a list of objects with type T
     */
    default ListSerializer<T> filteredListOf(Consumer<String> onError) {
        return new ListSerializer<>(this, str -> {
            onError.accept(str);
            return false;
        });
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
        return new MapSerializer<>(keySerializer, this, (key, str) -> false);
    }

    /**
     * Creates a map serializer from this serializer using the given key serializer which does not require all contained objects to (de-)serialize successfully
     * @param onError A callback to send error text whenever an object fails to serialize
     * @return A serializer for map with K keys and T values
     */
    default MapSerializer<String, T> filteredMapOf(BiConsumer<String, String> onError) {
        return new MapSerializer<>(InlineSerializer.RAW, this, (key, str) -> {
            onError.accept(key, str);
            return false;
        });
    }

    /**
     * Creates a map serializer from this serializer using the given key serializer which does not require all contained objects to (de-)serialize successfully
     * @param keySerializer The serializer to use to serialize the keys in the map to Strings
     * @param onError A callback to send error text whenever an object fails to serialize
     * @return A serializer for map with K keys and T values
     * @param <K> The type of values for the keys in the map
     */
    default <K> MapSerializer<K, T> filteredMapOf(InlineSerializer<K> keySerializer, BiConsumer<K, String> onError) {
        return new MapSerializer<>(keySerializer, this, (key, str) -> {
            onError.accept(key, str);
            return false;
        });
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

    /**
     * Creates a new serializer which maps data to another type before serialization and after serialization
     * @param getter A function which converts the mapped type to the serializer's type
     * @param construct A function which constructs the mapped type from the serializer's output
     * @param <O> The type to map to
     * @return A new serializer of the mapped type.
     */
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


    /**
     * Creates a new serializer which maps data to a blob
     * @return A new serializer of the mapped type.
     */
    default Serializer<T> mapToBlob(Codec codec) {
        return new Serializer<T>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    codec.encode(context, Serializer.this, value, bos);
                    return BLOB.serialize(context, ByteBuffer.wrap(bos.toByteArray()));
                } catch (IOException ex) {
                    return SerializeResult.failure("Unable to write a value to a blob! " + ex.getMessage());
                }
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                try(ByteBufferInputStream bis = new ByteBufferInputStream(context.asBlob(value))) {
                    return Serializer.this.deserialize(context, codec.decode(context, bis));
                } catch (IOException ex) {
                    return SerializeResult.failure("Unable to read a value from a blob! " + ex.getMessage());
                }
            }
        };
    }

    /**
     * Creates a serializer which reads from a map with the given key
     * @param key The map key to lookup when deserializing
     * @return A new serializer
     */
    default Serializer<T> fieldOf(String key) {
        return new Serializer<T>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return Serializer.this.serialize(context, value).flatMap(val -> {
                    Map<String, O> out = new HashMap<>();
                    out.put(key, val);
                    return context.toMap(out);
                });
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                O val;
                if(!context.isMap(value) || (val = context.get(key, value)) == null) {
                    return SerializeResult.failure("Key " + key + " not found!");
                }
                return Serializer.this.deserialize(context, val);
            }
        };
    }

    /**
     * Creates a combined serializer with this serializer and another
     * @param other The second serializer to use
     * @return A new serializer which serializes a pair of objects
     * @param <O> The type of the second serializer
     */
    default <O> Serializer<Tuples.T2<T, O>> and(Serializer<O> other) {

        return new Serializer<Tuples.T2<T, O>>() {
            @Override
            public <O2> SerializeResult<O2> serialize(SerializeContext<O2> context, Tuples.T2<T, O> value) {

                SerializeResult<O2> out1 = Serializer.this.serialize(context, value.p1);
                if(!out1.isComplete()) {
                    return out1;
                }

                SerializeResult<O2> out2 = other.serialize(context, value.p2);
                if(!out2.isComplete()) {
                    return out2;
                }

                return SerializeResult.success(context.merge(out1.getOrThrow(), out2.getOrThrow()));
            }

            @Override
            public <O2> SerializeResult<Tuples.T2<T, O>> deserialize(SerializeContext<O2> context, O2 value) {

                return Serializer.this.deserialize(context, value).and(other.deserialize(context, value));
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
    Serializer<Float> FLOAT = NumberSerializer.forFloat(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    Serializer<Double> DOUBLE = NumberSerializer.forDouble(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    Serializer<Boolean> BOOLEAN = BooleanSerializer.RAW.or(BooleanSerializer.NUMBER).or(BooleanSerializer.STRING);

    Serializer<BigInteger> BIG_INTEGER = new Serializer<BigInteger>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, BigInteger value) {
            return SerializeResult.success(context.toNumber(value));
        }

        @Override
        public <O> SerializeResult<BigInteger> deserialize(SerializeContext<O> context, O value) {
            switch (context.getType(value)) {
                case STRING:
                    return SerializeResult.success(new BigInteger(context.asString(value)));
                case NUMBER:
                    Number num = context.asNumber(value);
                    BigInteger out;
                    if(num instanceof BigInteger) {
                        out = (BigInteger) num;
                    } else if(ConfigPrimitive.isInteger(num)) {
                        out = BigInteger.valueOf(num.longValue());
                    } else {
                        out = BigInteger.valueOf((long) num.doubleValue());
                    }
                    return SerializeResult.success(out);
                default:
                    return SerializeResult.failure("Unable to read " + value + " as a BigInteger!");
            }
        }
    };
    Serializer<BigDecimal> BIG_DECIMAL = new Serializer<BigDecimal>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, BigDecimal value) {
            return SerializeResult.success(context.toNumber(value));
        }

        @Override
        public <O> SerializeResult<BigDecimal> deserialize(SerializeContext<O> context, O value) {
            switch (context.getType(value)) {
                case STRING:
                    return SerializeResult.success(new BigDecimal(context.asString(value)));
                case NUMBER:
                    Number num = context.asNumber(value);
                    BigDecimal out;
                    if(num instanceof BigDecimal) {
                        out = (BigDecimal) num;
                    } else if(ConfigPrimitive.isInteger(num)) {
                        out = BigDecimal.valueOf(num.longValue());
                    } else {
                        out = BigDecimal.valueOf((long) num.doubleValue());
                    }
                    return SerializeResult.success(out);
                default:
                    return SerializeResult.failure("Unable to read " + value + " as a BigDecimal!");
            }
        }
    };

    Serializer<ByteBuffer> BLOB = new Serializer<ByteBuffer>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, ByteBuffer value) {
            return SerializeResult.success(context.toBlob(value));
        }

        @Override
        public <O> SerializeResult<ByteBuffer> deserialize(SerializeContext<O> context, O value) {
            return SerializeResult.success(context.asBlob(value));
        }
    };


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

    Serializer<Object> NULL = new Serializer<Object>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, Object value) {
            return SerializeResult.success(context.nullValue());
        }

        @Override
        public <O> SerializeResult<Object> deserialize(SerializeContext<O> context, O value) {
            return SerializeResult.success(null);
        }
    };

}
