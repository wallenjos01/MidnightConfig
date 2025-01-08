package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Decoder {

    /**
     * Decodes a value from the given input using the given charset and context
     * @param context The context by which to decode
     * @param stream The stream to read input from
     * @param charset The charset to decode data from
     * @param <T> The type of values to encode
     * @throws DecodeException If decoding fails
     * @throws IOException If reading from the stream fails
     */
    <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) throws DecodeException, IOException;

    /**
     * Decodes a value from the given string using the given context and UTF-8 encoding
     * @param context The context by which to decode
     * @param string The string to read
     * @param <T> The type of values to encode
     * @throws DecodeException if decoding fails
     */
    default <T> T decode(@NotNull SerializeContext<T> context, @NotNull String string) throws DecodeException {
        try {
            return decode(context, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException ex) {
            throw new DecodeException("An IOException was thrown while data was being read! " + ex.getMessage());
        }
    }

    /**
     * Decodes a value from the given input stream using the given context and UTF-8 encoding
     * @param context The context by which to decode
     * @param stream The stream to read input from
     * @param <T> The type of values to encode
     * @throws DecodeException if decoding fails
     * @throws IOException If reading from the stream fails
     */
    default <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream) throws DecodeException, IOException {
        return decode(context, stream, StandardCharsets.UTF_8);
    }

    /**
     * Decodes a value from the given string using the given context and UTF-8 encoding, then deserializes it with the given serializer
     * @param context The context by which to decode
     * @param serializer The serializer by which to deserialize the decoded data
     * @param string The encoded, serialized data to decode
     * @return The result of deserialization
     * @param <T> The type of values to decode
     * @param <O> The type of data to deserialize to
     * @throws DecodeException if decoding fails
     */
    default <T, O> SerializeResult<O> decode(@NotNull SerializeContext<T> context, @NotNull Serializer<O> serializer, @NotNull String string) throws DecodeException {

        try {
            return serializer.deserialize(context, decode(context, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))));
        } catch (IOException ex) {
            throw new DecodeException("An IOException occurred while decoding a String! " + ex.getMessage());
        }
    }

    /**
     * Decodes a value from the given string using the given context and UTF-8 encoding, then deserializes it with the given serializer
     * @param context The context by which to decode
     * @param serializer The serializer by which to deserialize the decoded data
     * @param stream The encoded, serialized data to decode
     * @return The result of deserialization
     * @param <T> The type of values to decode
     * @param <O> The type of data to deserialize to
     * @throws DecodeException if decoding fails
     */
    default <T, O> SerializeResult<O> decode(SerializeContext<T> context, Serializer<O> serializer, InputStream stream) throws DecodeException, IOException {

        return serializer.deserialize(context, decode(context, stream));
    }

}
