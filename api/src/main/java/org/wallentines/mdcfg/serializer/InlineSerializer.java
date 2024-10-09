package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

/**
 * Serializes data to and from Strings
 */
public interface InlineSerializer<T> extends Serializer<T> {

    @Override
    default <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
        return writeString(value).flatMap(context::toString);
    }

    @Override
    default <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
        return Serializer.STRING.deserialize(context, value).map(this::readString);
    }

    SerializeResult<T> readString(String str);

    SerializeResult<String> writeString(T value);

    /**
     * Creates a new inline serializer using the given functions
     * @param serialize The function to use to turn a value into a String
     * @param deserialize The function to use to turn a String into a value
     * @return A new Serializer
     * @param <T> The type of data to serialize
     */
    static <T> InlineSerializer<T> of(Function<T, String> serialize, Function<String, T> deserialize) {
        return new InlineSerializer<>() {
            @Override
            public SerializeResult<T> readString(String str) {
                return SerializeResult.ofNullable(deserialize.apply(str), "Unable to read string");
            }
            @Override
            public SerializeResult<String> writeString(T value) {
                return SerializeResult.ofNullable(serialize.apply(value), "Unable to write string");
            }
        };
    }

    InlineSerializer<String> RAW = of(str -> str, str -> str);

}
