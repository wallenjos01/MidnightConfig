package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Codec {

    <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset);

    <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset);

    default <T> T decode(SerializeContext<T> context, String string) {
        return decode(context, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    default <T> T decode(SerializeContext<T> context, InputStream data) {
        return decode(context, data, StandardCharsets.UTF_8);
    }

    default <T> String encodeToString(SerializeContext<T> context, T input) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encode(context, input, os, StandardCharsets.UTF_8);

        return os.toString(StandardCharsets.UTF_8);
    }

}
