package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public interface Codec {

    <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset);

    <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset);


    default <T, O> void encode(SerializeContext<T> context, Serializer<O> serializer, O data, OutputStream stream) {

        encode(context, serializer.serialize(context, data).getOrThrow(), stream, StandardCharsets.UTF_8);
    }

    default <T, O> void encode(SerializeContext<T> context, Serializer<O> serializer, O data, OutputStream stream, Charset charset) {

        encode(context, serializer.serialize(context, data).getOrThrow(), stream, charset);
    }

    default <T> T decode(SerializeContext<T> context, String string) {
        return decode(context, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    default <T> T decode(SerializeContext<T> context, InputStream data) {
        return decode(context, data, StandardCharsets.UTF_8);
    }

    default <T, O> SerializeResult<O> decode(SerializeContext<T> context, Serializer<O> serializer, String data) {

        return serializer.deserialize(context, decode(context, new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))));
    }

    default <T, O> SerializeResult<O> decode(SerializeContext<T> context, Serializer<O> serializer, InputStream data) {

        return serializer.deserialize(context, decode(context, data));
    }

    default <T> String encodeToString(SerializeContext<T> context, T input) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encode(context, input, os, StandardCharsets.UTF_8);

        return os.toString(StandardCharsets.UTF_8);
    }


}
