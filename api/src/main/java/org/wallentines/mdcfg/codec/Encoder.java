package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Encoder {

    /**
     * Encodes the given input to the given output stream using the given charset and context
     * @param context The context by which to encode
     * @param input The value to encode
     * @param stream The stream to write output to
     * @param charset The charset to encode data with
     * @param <T> The type of values to encode
     * @throws EncodeException If encoding fails
     * @throws IOException If writing to the stream fails
     */
    <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws EncodeException, IOException;

    /**
     * Encodes the given value to the given output stream using the given context and UTF-8 encoding
     * @param context The context by which to encode
     * @param input The value to encode
     * @param stream The stream to write output to
     * @param <T> The type of values to encode
     * @throws EncodeException If encoding fails
     * @throws IOException If writing to the stream fails
     */
    default <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream) throws EncodeException, IOException {
        encode(context, input, stream, StandardCharsets.UTF_8);
    }

    /**
     * Serializes a value, then encodes the given value to the given output stream using the given context and UTF-8 encoding
     * @param context The context by which to encode
     * @param serializer The serializer by which to serialize the value
     * @param data The value to encode
     * @param stream The stream to write output to
     * @param <T> The type of values to encode
     * @throws EncodeException If encoding fails
     * @throws IOException If writing to the stream fails
     */
    default <T, O> void encode(@NotNull SerializeContext<T> context, @NotNull Serializer<O> serializer, O data, @NotNull OutputStream stream) throws EncodeException, IOException {

        encode(context, serializer.serialize(context, data).getOrThrow(), stream, StandardCharsets.UTF_8);
    }

    /**
     * Serializes a value, then encodes the given value to the given output stream using the given charset and context
     * @param context The context by which to encode
     * @param serializer The serializer by which to serialize the value
     * @param data The value to encode
     * @param stream The stream to write output to
     * @param charset The charset to use for encoding
     * @param <T> The type of values to encode
     * @param <O> The type of data to serialize from
     * @throws EncodeException If encoding fails
     * @throws IOException If writing to the stream fails
     */
    default <T, O> void encode(@NotNull SerializeContext<T> context, @NotNull Serializer<O> serializer, O data, @NotNull OutputStream stream, @NotNull Charset charset) throws EncodeException, IOException {

        encode(context, serializer.serialize(context, data).getOrThrow(), stream, charset);
    }

    /**
     * Encodes the given value to a string according to the given context
     * @param context The context by which to encode data
     * @param input The input data
     * @return The input encoded as a String
     * @param <T> The type of values to encode
     * @throws EncodeException If encoding fails
     */
    default <T> String encodeToString(SerializeContext<T> context, T input) throws EncodeException {

        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            encode(context, input, os, StandardCharsets.UTF_8);

            return os.toString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new EncodeException("An IOException occurred while encoding data to a String! " + ex.getMessage());
        }
    }


}
