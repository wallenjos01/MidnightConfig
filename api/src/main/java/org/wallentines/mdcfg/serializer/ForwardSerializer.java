package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

public interface ForwardSerializer<T> {

    /**
     * Serializes an object with type T into an object of type O, using the given context
     * @param context The context by which to convert the given value to another type
     * @param value The value to serialize
     * @return A SerializeResult containing the serialized object, or an error String if serialization failed
     * @param <O> The type to serialize
     */
    <O> SerializeResult<O> serialize(SerializeContext<O> context, T value);


    @Deprecated
    default <O> ForwardSerializer<O> map(Function<? super O, SerializeResult<? extends T>> mapper) {
        return mapForward(mapper);
    }

    /**
     * Maps this forward serializer to another type
     * @param mapper A function to convert the input to before serializing
     * @return A new forward serializer
     * @param <O> The new type to serialize
     */
    default <O> ForwardSerializer<O> mapForward(Function<? super O, SerializeResult<? extends T>> mapper) {
        return new ForwardSerializer<O>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                SerializeResult<? extends T> result = mapper.apply(value);
                if(!result.isComplete()) {
                    return SerializeResult.failure(result.getError());
                }
                return ForwardSerializer.this.serialize(context, result.getOrNull());
            }
        };
    }


    @Deprecated
    default <O> ForwardSerializer<O> flatMap(Function<? super O, ? extends T> mapper) {
        return flatMapForward(mapper);
    }

    /**
     * Maps this forward serializer to another type
     * @param mapper A function to convert the input to before serializing
     * @return A new forward serializer
     * @param <O> The new type to serialize
     */
    default <O> ForwardSerializer<O> flatMapForward(Function<? super O, ? extends T> mapper) {
        return new ForwardSerializer<O>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                return ForwardSerializer.this.serialize(context, mapper.apply(value));
            }
        };
    }


    /**
     * Creates a serializer from the given ForwardSerializer which will fail on each call to deserialize()
     * @param serializer The ForwardSerializer to wrap
     * @return A new serializer
     * @param <T> The type to serialize
     */
    static <T> Serializer<T> oneWay(ForwardSerializer<T> serializer) {
        return new Serializer<T>() {
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return SerializeResult.failure("Serializing not supported!");
            }

            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return serializer.serialize(context, value);
            }
        };
    }

}
