package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public interface Codec {

    /**
     * Encodes the given input to the given output stream using the given charset and context
     * @param context The context by which to encode
     * @param input The value to encode
     * @param stream The stream to write output to
     * @param charset The charset to encode data with
     * @param <T> The type of values to encode
     */
    <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset);

    /**
     * Decodes a value from the given input using the given charset and context
     * @param context The context by which to decode
     * @param stream The stream to read input from
     * @param charset The charset to decode data from
     * @param <T> The type of values to encode
     * @throws DecodeException if decoding fails
     */
    <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset) throws DecodeException;

    /**
     * Encodes the given value to the given output stream using the given context and UTF-8 encoding
     * @param context The context by which to encode
     * @param input The value to encode
     * @param stream The stream to write output to
     * @param <T> The type of values to encode
     */
    default <T> void encode(SerializeContext<T> context, T input, OutputStream stream) {

        encode(context, input, stream, StandardCharsets.UTF_8);
    }

    /**
     * Serializes a value, then encodes the given value to the given output stream using the given context and UTF-8 encoding
     * @param context The context by which to encode
     * @param serializer The serializer by which to serialize the value
     * @param data The value to encode
     * @param stream The stream to write output to
     * @param <T> The type of values to encode
     */
    default <T, O> void encode(SerializeContext<T> context, Serializer<O> serializer, O data, OutputStream stream) {

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
     */
    default <T, O> void encode(SerializeContext<T> context, Serializer<O> serializer, O data, OutputStream stream, Charset charset) {

        encode(context, serializer.serialize(context, data).getOrThrow(), stream, charset);
    }

    /**
     * Decodes a value from the given string using the given context and UTF-8 encoding
     * @param context The context by which to decode
     * @param string The string to read
     * @param <T> The type of values to encode
     * @throws DecodeException if decoding fails
     */
    default <T> T decode(SerializeContext<T> context, String string) throws DecodeException {
        return decode(context, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decodes a value from the given input stream using the given context and UTF-8 encoding
     * @param context The context by which to decode
     * @param stream The stream to read input from
     * @param <T> The type of values to encode
     * @throws DecodeException if decoding fails
     */
    default <T> T decode(SerializeContext<T> context, InputStream stream) throws DecodeException {
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
    default <T, O> SerializeResult<O> decode(SerializeContext<T> context, Serializer<O> serializer, String string) throws DecodeException {

        return serializer.deserialize(context, decode(context, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))));
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
    default <T, O> SerializeResult<O> decode(SerializeContext<T> context, Serializer<O> serializer, InputStream stream) throws DecodeException {

        return serializer.deserialize(context, decode(context, stream));
    }

    /**
     * Encodes the given value to a string according to the given context
     * @param context The context by which to encode data
     * @param input The input data
     * @return The input encoded as a String
     * @param <T> The type of values to encode
     */
    default <T> String encodeToString(SerializeContext<T> context, T input) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encode(context, input, os, StandardCharsets.UTF_8);

        return os.toString(StandardCharsets.UTF_8);
    }


}
