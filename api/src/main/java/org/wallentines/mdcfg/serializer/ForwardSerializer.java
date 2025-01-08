package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

public interface ForwardSerializer<T> {

    /**
     * Serializes an object with type T into an object of type O, using the given context
     * @param context The context by which to convert the given value to another type
     * @param value The value to serialize
     * @return A SerializeResult containing the serialized object, or an error String if serialization failed
     * @param <O> The type to serialize the value into
     */
    <O> SerializeResult<O> serialize(SerializeContext<O> context, T value);

    default <O> ForwardSerializer<O> map(Function<? super O, SerializeResult<? extends T>> mapper) {
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

    default <O> ForwardSerializer<O> flatMap(Function<? super O, ? extends T> mapper) {
        return new ForwardSerializer<O>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                return ForwardSerializer.this.serialize(context, mapper.apply(value));
            }
        };
    }

}
