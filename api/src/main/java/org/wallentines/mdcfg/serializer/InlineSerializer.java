package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

/**
 * Serializes data to and from Strings
 */
public interface InlineSerializer<T> extends Serializer<T> {

    @Override
    default <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
        String out = writeString(value);
        return SerializeResult.ofNullable(context.toString(out));
    }

    @Override
    default <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
        return Serializer.STRING.deserialize(context, value).map(str -> SerializeResult.ofNullable(readString(str)));
    }

    /**
     * Parses a value from a String
     * @param str The string to parse
     * @return A parsed value, or null if parsing fails
     */
    T readString(String str);

    /**
     * Writes a value to a String
     * @param value The string to write
     * @return A serialized value, or null if serialization fails
     */
    String writeString(T value);

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
            public String writeString(T value) {
                return serialize.apply(value);
            }
            @Override
            public T readString(String str) {
                return deserialize.apply(str);
            }
        };
    }

    InlineSerializer<String> RAW = of(str -> str, str -> str);

}
