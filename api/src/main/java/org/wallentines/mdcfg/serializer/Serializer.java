package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.*;
import org.wallentines.mdcfg.codec.Codec;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.EncodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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
     * Creates an entry for use in an ObjectSerializer using the given key and getter
     * @param key The key to use for lookups and saving
     * @param getter A function to retrieve an instance of T from an instance of O when serializing
     * @return An entry for use in ObjectSerializer.create()
     * @param <O> The type of object to serialize
     */
    default <O> ObjectSerializer.Entry<T, O> entry(String key, Functions.F2<O, SerializeContext<?>, T> getter) {
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
        return new ListSerializer<>(this, (err) -> false);
    }

    /**
     * Creates a list serializer from this serializer which does not require all contained objects to (de-)serialize successfully
     * @param onError A callback to send error text whenever an object fails to serialize
     * @return A serializer for a list of objects with type T
     */
    default ListSerializer<T> filteredListOf(Consumer<Throwable> onError) {
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
    default MapSerializer<String, T> filteredMapOf(BiConsumer<String, Throwable> onError) {
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
    default <K> MapSerializer<K, T> filteredMapOf(InlineSerializer<K> keySerializer, BiConsumer<K, Throwable> onError) {
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
        return new Serializer<T>() {
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
     * Creates a serializer for an Either object
     * @param left The serializer to use if the left value is present
     * @param right The serializer to user if the right value is present
     * @return A new serializer
     * @param <L> The left type
     * @param <R> The right type
     */
    static <L, R> Serializer<Either<L, R>> either(Serializer<L> left, Serializer<R> right) {
        return new Serializer<Either<L, R>>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, Either<L, R> value) {
                if(value == null) {
                    return SerializeResult.failure("either is null");
                }
                if (value.hasLeft()) {
                    return left.serialize(context, value.left());
                } else {
                    return right.serialize(context, value.right());
                }
            }

            @Override
            public <O> SerializeResult<Either<L, R>> deserialize(SerializeContext<O> context, O value) {
                return left.deserialize(context, value)
                        .flatMap(Either::<L, R>left)
                        .mapError(() -> right.deserialize(context, value)
                                .flatMap(Either::<L, R>right));
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
    default <O> Serializer<O> map(Function<O, SerializeResult<T>> getter, Function<T, SerializeResult<O>> construct) {
        return new Serializer<>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                return getter.apply(value).map(t -> Serializer.this.serialize(context, t));
            }

            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                return Serializer.this.deserialize(context, value).map(construct);
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
    default <O> Serializer<O> flatMap(Function<O, T> getter, Function<T, O> construct) {
        return new Serializer<O>() {
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
     * Creates a new serializer which maps data to a blob encoded using the given codec.
     * @return A new serializer of the mapped type.
     */
    default Serializer<T> mapToBlob(Codec codec) {
        return new Serializer<T>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    codec.encode(context, Serializer.this, value, bos);
                    return BLOB.serialize(context, ByteBuffer.wrap(bos.toByteArray()));
                } catch (IOException | EncodeException ex) {
                    return SerializeResult.failure("Unable to write a value to a blob! " + ex.getMessage());
                }
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return context.asBlob(value).map(buf -> {
                    try(ByteBufferInputStream bis = new ByteBufferInputStream(buf)) {
                        return Serializer.this.deserialize(context, codec.decode(context, bis));
                    } catch (IOException | DecodeException ex) {
                        return SerializeResult.failure("Unable to read a value from a blob! " + ex.getMessage());
                    }
                });
            }
        };
    }

    /**
     * Creates a new serializer which maps data to a string encoded using the given codec.
     * @return A new serializer of the mapped type.
     */
    default Serializer<T> mapToString(Codec codec) {
        return new Serializer<T>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {

                try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    codec.encode(context, Serializer.this, value, bos);
                    return STRING.serialize(context, bos.toString());
                } catch (IOException | EncodeException ex) {
                    return SerializeResult.failure("Unable to write a value to a string! " + ex.getMessage());
                }
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return context.asString(value).map(str -> Serializer.this.deserialize(context, codec.decode(context, str)));
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

    default <O> Serializer<O> dispatch(Function<T, Serializer<O>> dispatcher, Function<O, T> reverse) {
        return new Serializer<O>() {
            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                SerializeResult<T> t = Serializer.this.deserialize(context, value);
                if(!t.isComplete()) return SerializeResult.failure(t.getError());
                return dispatcher.apply(t.getOrThrow()).deserialize(context, value);
            }

            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                T t = reverse.apply(value);
                SerializeResult<O1> o = Serializer.this.serialize(context, t);
                if(!o.isComplete()) return o;

                SerializeResult<O1> o2 = dispatcher.apply(t).serialize(context, value);
                if(!o2.isComplete()) return o2;

                return SerializeResult.success(context.merge(o.getOrThrow(), o2.getOrThrow()));
            }
        };
    }

    default <O> Serializer<O> cast(Class<T> source, Class<O> dest) {
        return new Serializer<O>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                if(!source.isAssignableFrom(value.getClass())) return SerializeResult.failure("Expected value of type " + source);
                return Serializer.this.serialize(context, source.cast(value));
            }

            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                return Serializer.this.deserialize(context, value).cast(dest);
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
            if(value == null) return SerializeResult.failure("String is null!");
            return SerializeResult.success(context.toString(value));
        }
        @Override
        public <O> SerializeResult<String> deserialize(SerializeContext<O> context, O value) {
            return context.asString(value);
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
                    return SerializeResult.success(new BigInteger(context.asString(value).getOrThrow()));
                case NUMBER:
                    Number num = context.asNumber(value).getOrThrow();
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
                    return SerializeResult.success(new BigDecimal(context.asString(value).getOrThrow()));
                case NUMBER:
                    Number num = context.asNumber(value).getOrThrow();
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

            return context.asBlob(value).mapError(() -> context.asString(value).map(val -> {
                Base64.Decoder dec = Base64.getDecoder();
                try {
                    return SerializeResult.success(ByteBuffer.wrap(dec.decode(val)));
                } catch (IllegalArgumentException e) {
                    return SerializeResult.failure("String was not in base64! " + e.getMessage());
                }
            }));
        }
    };


    Serializer<UUID> UUID = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, java.util.UUID value) {
            return SerializeResult.ofNullable(value, "Unable to save " + value + " as a UUID!").map(uuid -> SerializeResult.ofNullable(context.toString(value.toString())));
        }

        @Override
        public <O> SerializeResult<java.util.UUID> deserialize(SerializeContext<O> context, O value) {

            return context.asString(value).map(str -> {
                try {
                    return SerializeResult.success(java.util.UUID.fromString(str));
                } catch (IllegalArgumentException ex) {
                    return SerializeResult.failure("Unable to parse " + str + " as a UUID!");
                }
            });
        }
    };


    InlineSerializer<Path> PATH = InlineSerializer.of(Path::toString, Paths::get);

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
