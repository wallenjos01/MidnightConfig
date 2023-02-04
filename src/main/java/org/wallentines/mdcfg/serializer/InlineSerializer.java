package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

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

    T readString(String str);

    String writeString(T value);

    static <T> InlineSerializer<T> of(Function<String, T> create, Function<T, String> get) {
        return new InlineSerializer<>() {
            @Override
            public T readString(String str) {
                return create.apply(str);
            }

            @Override
            public String writeString(T value) {
                return get.apply(value);
            }
        };
    }

    InlineSerializer<String> RAW = of(str -> str, str -> str);

}
