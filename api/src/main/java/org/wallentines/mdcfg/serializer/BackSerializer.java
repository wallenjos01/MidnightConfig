package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

public interface BackSerializer<T> {

    /**
     * Deserializes an object from type O into an object of type T, using the given context
     * @param context The context by which to convert the given value to another type
     * @param value The value to deserialize
     * @return A SerializeResult containing the deserialized object, or an error String if deserialization failed
     * @param <O> The type to deserialize the value from
     */
    <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value);


    /**
     * Maps this forward serializer to another type
     * @param mapper A function to convert the input to before serializing
     * @return A new forward serializer
     * @param <O> The new type to serialize
     */
    default <O> BackSerializer<O> mapBack(Function<? super T, SerializeResult<? extends O>> mapper) {
        return new BackSerializer<O>() {
            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                return BackSerializer.this.deserialize(context, value).map(mapper);
            }
        };
    }

    /**
     * Maps this forward serializer to another type
     * @param mapper A function to convert the input to before serializing
     * @return A new forward serializer
     * @param <O> The new type to serialize
     */
    default <O> BackSerializer<O> flatMapBack(Function<? super T, ? extends O> mapper) {
        return new BackSerializer<O>() {
            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                return BackSerializer.this.deserialize(context, value).flatMap(mapper);
            }
        };
    }

    /**
     * Creates a serializer from the given BackSerializer which will fail on each call to serialize()
     * @param serializer The BackSerializer to wrap
     * @return A new serializer
     * @param <T> The type to deserialize
     */
    static <T> Serializer<T> oneWay(BackSerializer<T> serializer) {
        return new Serializer<T>() {
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return serializer.deserialize(context, value);
            }

            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return SerializeResult.failure("Serializing not supported!");
            }
        };
    }

}
