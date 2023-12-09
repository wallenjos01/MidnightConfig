package org.wallentines.mdcfg.serializer;

import java.util.function.BiFunction;

public interface InlineContextSerializer<T,C> extends ContextSerializer<T,C> {


    @Override
    default <O> SerializeResult<O> serialize(SerializeContext<O> context, T value, C ctx) {
        String out = writeString(value, ctx);
        return SerializeResult.ofNullable(context.toString(out));
    }

    @Override
    default <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value, C ctx) {
        return Serializer.STRING.deserialize(context, value).map(str -> SerializeResult.ofNullable(readString(str, ctx)));
    }

    /**
     * Parses a value from a String
     * @param str The string to parse
     * @param context The context to deserialize against
     * @return A parsed value, or null if parsing fails
     */
    T readString(String str, C context);

    /**
     * Writes a value to a String
     * @param value The string to write
     * @param context The context to serialize against
     * @return A serialized value, or null if serialization fails
     */
    String writeString(T value, C context);

    /**
     * Creates a new inline serializer using the given functions
     * @param serialize The function to use to turn a value into a String
     * @param deserialize The function to use to turn a String into a value
     * @return A new Serializer
     * @param <T> The type of data to serialize
     */
    static <T,C> InlineContextSerializer<T, C> of(BiFunction<T, C, String> serialize, BiFunction<String, C, T> deserialize) {
        return new InlineContextSerializer<>() {
            @Override
            public String writeString(T value, C ctx) {
                return serialize.apply(value, ctx);
            }
            @Override
            public T readString(String str, C ctx) {
                return deserialize.apply(str, ctx);
            }
        };
    }

    static <C> InlineContextSerializer<String,C> raw() {
        return of((str,c) -> str, (str,c) -> str);
    }

}
