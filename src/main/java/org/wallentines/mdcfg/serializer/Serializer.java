package org.wallentines.mdcfg.serializer;

import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface Serializer<T> {

    <O> SerializeResult<O> serialize(SerializeContext<O> context, T value);

    <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value);

    default <O> ObjectSerializer.Entry<T, O> entry(String key, Function<O, T> getter) {
        return ObjectSerializer.entry(key, this, getter);
    }

    default ListSerializer<T> listOf() {
        return new ListSerializer<>(this);
    }

    default MapSerializer<String, T> mapOf() {
        return mapOf(InlineSerializer.RAW);
    }

    default <K> MapSerializer<K, T> mapOf(InlineSerializer<K> keySerializer) {
        return new MapSerializer<>(keySerializer, this);
    }

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
    Serializer<Float> FLOAT = NumberSerializer.forFloat(Float.MIN_VALUE, Float.MAX_VALUE);
    Serializer<Double> DOUBLE = NumberSerializer.forDouble(Double.MIN_VALUE, Double.MAX_VALUE);
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
