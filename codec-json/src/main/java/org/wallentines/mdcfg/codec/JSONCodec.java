package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A {@link Codec Codec} for JSON data
 */
public class JSONCodec implements Codec {

    private final boolean shouldIndent;
    private final String indent;

    /**
     * Creates a JSON codec which does not indent or add newlines
     */
    public JSONCodec() {
        this(0);
    }

    /**
     * Creates a JSON codec which adds newlines and indents a with a given amount of spaces
     * @param indentAmount The amount of spaces to indent each new level with
     */
    public JSONCodec(int indentAmount) {
        this.indent = indentAmount <= 0 ? "" : " ".repeat(indentAmount);
        this.shouldIndent = indentAmount > 0;
    }

    /**
     * Creates a JSON codec which does not indent or add newlines
     * @return A new JSON codec
     */
    public static JSONCodec minified() {
        return new JSONCodec(0);
    }

    /**
     * Creates a JSON codec which adds newlines and indents with 4 spaces
     * @return A new JSON codec
     */
    public static JSONCodec readable() {
        return new JSONCodec(4);
    }

    /**
     * Creates a JSON file codec using only the ".json" extension and a readable codec
     */
    public static FileCodec fileCodec() {
        return fileCodec(readable());
    }

    /**
     * Creates a JSON file codec using only the ".json" extension and the given JSON codec
     */
    public static FileCodec fileCodec(JSONCodec codec) {
        return new FileCodec(codec, "json");
    }

    /**
     * Loads data as a ConfigObject from the given String
     * @param string The encoded data to read
     * @return A decoded ConfigObject
     * @throws DecodeException If the data could not be decoded
     */
    public static ConfigObject loadConfig(String string) {
        return minified().decode(ConfigContext.INSTANCE, string);
    }

    /**
     * Loads data as a ConfigObject from the given input stream
     * @param stream The encoded data to read
     * @return A decoded ConfigObject
     * @throws DecodeException If the data could not be decoded
     */
    public static ConfigObject loadConfig(InputStream stream) throws IOException {
        return minified().decode(ConfigContext.INSTANCE, stream);
    }

    @Override
    public <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws EncodeException, IOException {
        new Encoder<>(context).encode(input, stream, charset);
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) throws DecodeException, IOException {
        return new Decoder<>(context).decode(stream, charset);
    }


    private class Encoder<T> {
        private final SerializeContext<T> context;

        public Encoder(SerializeContext<T> context) {
            this.context = context;
        }

        public void encode(T section, OutputStream stream, Charset charset) throws IOException {

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
            encode(section, "", writer);
            writer.close();

        }

        private void encodeMap(T section, String prefix, BufferedWriter writer) throws IOException {

            if (!context.isMap(section)) throw new IllegalArgumentException("Not a map: " + section);

            Collection<String> keys = context.getOrderedKeys(section);
            String nextPrefix = prefix + indent;

            writer.write("{");
            if(keys.isEmpty()) {
                if(shouldIndent) writer.write(" ");
                writer.write("}");
                return;
            }

            if(shouldIndent) writer.write("\n");
            int index = 0;

            for (String key : keys) {

                if (index++ > 0) {
                    writer.write(",");
                    if(shouldIndent) writer.write("\n");
                }

                T value = context.get(key, section);
                writer.write(nextPrefix + "\"" + encodeString(key) + "\":");
                if(shouldIndent) writer.write(" ");
                encode(value, nextPrefix, writer);
            }
            if(shouldIndent) writer.write("\n");
            writer.write(prefix + "}");
        }

        private String encodeString(String s) {

            StringBuilder builder = new StringBuilder();
            if(s == null) return builder.toString();

            PrimitiveIterator.OfInt it = ((CharSequence) s).chars().iterator();

            while(it.hasNext()) {
                int c = it.nextInt();
                switch (c) {
                    case '\\':
                        builder.append("\\\\");
                        break;
                    case '"':
                        builder.append("\\\"");
                        break;
                    case '\n':
                        builder.append("\\n");
                        break;
                    case '\r':
                        builder.append("\\r");
                        break;
                    case '\f':
                        builder.append("\\f");
                        break;
                    case '\b':
                        builder.append("\\b");
                        break;
                    case '\t':
                        builder.append("\\t");
                        break;
                    default:
                        builder.appendCodePoint(c);
                }
            }

            return builder.toString();
        }

        private void encodeList(T value, String prefix, BufferedWriter writer) throws IOException {

            SerializeResult<Collection<T>> collectionResult = context.asList(value);
            if(!collectionResult.isComplete()) {
                throw new EncodeException("Not a list", collectionResult.getError());
            }

            Collection<T> collection = collectionResult.getOrThrow(EncodeException::new);
            String nextPrefix = prefix + indent;

            writer.write("[");
            if(collection.isEmpty()) {
                if(shouldIndent) writer.write(" ");
                writer.write("]");
                return;
            }

            if(shouldIndent) writer.write("\n");

            int index = 0;
            for (T t : collection) {

                if (index++ > 0) {
                    writer.write(",");
                    if(shouldIndent) writer.write("\n");
                }
                writer.write(nextPrefix);
                encode(t, nextPrefix, writer);
            }
            if(shouldIndent) writer.write("\n");
            writer.write(prefix + "]");
        }

        private void encode(T value, String prefix, BufferedWriter writer) throws IOException {

            switch (context.getType(value)) {
                case STRING:
                    writer.write("\"");
                    writer.write(encodeString(context.asString(value).getOrThrow(EncodeException::new)));
                    writer.write("\"");
                    break;
                case NUMBER:
                    writer.write(context.asNumber(value).getOrThrow(EncodeException::new).toString());
                    break;
                case BOOLEAN:
                    writer.write(context.asBoolean(value).getOrThrow(EncodeException::new).toString());
                    break;
                case BLOB:
                    String str = Base64.getEncoder().encode(context.asBlob(value).getOrThrow(EncodeException::new)).asCharBuffer().toString();
                    writer.write("\"");
                    writer.write(str);
                    writer.write("\"");
                    break;
                case LIST:
                    encodeList(value, prefix, writer);
                    break;
                case MAP:
                    encodeMap(value, prefix, writer);
                    break;
                case NULL:
                    writer.write("null");
                    break;
                default:
                    throw new EncodeException("Unable to serialize " + value + "!");
            }

        }
    }

    private static class Decoder<T> {

        private final SerializeContext<T> context;
        private int lastReadChar;

        public Decoder(SerializeContext<T> context) {
            this.context = context;
        }

        public T decode(InputStream data, Charset charset) throws IOException {

            BufferedReader reader = new BufferedReader(new InputStreamReader(data, charset.newDecoder()));
            T out = decodeElement(reader);
            reader.close();
            return out;
        }

        public T decode(Reader reader) throws IOException {

            T out = decodeElement(reader);
            reader.close();
            return out;
        }

        private void skipWhitespace(Reader reader) throws IOException {
            while(lastReadChar <= 32) {
                lastReadChar = reader.read();
                if(lastReadChar == -1) {
                    throw new DecodeException("Found EOF while attempting to parse JSON!");
                }
            }
        }

        private int nextReal(Reader reader) throws IOException {
            do {
                lastReadChar = reader.read();
            } while(lastReadChar <= 32 && lastReadChar > -1);
            return lastReadChar;
        }

        private T decodeElement(Reader reader) throws IOException {

            skipWhitespace(reader);

            if(lastReadChar == '{') {
                return decodeMap(reader);
            }
            if(lastReadChar == '[') {
                return decodeList(reader);
            }
            if(lastReadChar == '"') {
                return decodeString(reader);
            }
            if(lastReadChar == '}' || lastReadChar == ']') { // Illegal Characters (in this context)
                throw new DecodeException("Found illegal character " + lastReadChar);
            }

            return decodePrimitive(reader);
        }

        private T decodeString(Reader reader) throws IOException {

            return context.toString(readString(reader));
        }

        private T decodePrimitive(Reader reader) throws IOException {

            StringBuilder output = new StringBuilder();

            do {
                if(lastReadChar > 127) {
                    throw new DecodeException("Found invalid character while reading a primitive!");
                }
                output.append((char) lastReadChar);
            }
            while((lastReadChar = reader.read()) > 32
                    && lastReadChar != '}'
                    && lastReadChar != ','
                    && lastReadChar != ']');

            String value = output.toString();

            if(value.equalsIgnoreCase("true")) {
                return context.toBoolean(true);
            }
            if(value.equalsIgnoreCase("false")) {
                return context.toBoolean(false);
            }
            if(value.equalsIgnoreCase("null")) {
                return context.nullValue();
            }

            String validDigits = "0123456789.-E";

            // Number
            if(value.endsWith(".") || value.split("\\.").length > 2 || hasInvalidChars(value, validDigits)) {
                throw new DecodeException("Unable to parse " + value + " as a number!");
            }

            Number out;
            if(value.indexOf('.') == -1) {

                try {
                    out = Long.parseLong(value);
                    long val = out.longValue();
                    if(val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                        out = out.intValue();
                    }

                } catch (NumberFormatException ex) {
                    out = new BigInteger(value);

                }

            } else {
                out = Double.parseDouble(value);
            }

            return context.toNumber(out);
        }

        private T decodeMap(Reader reader) throws IOException {

            if(lastReadChar != '{') {
                throw new DecodeException("Expected object to start with '{'");
            }

            Map<String, T> values = new LinkedHashMap<>();

            nextReal(reader);

            String lastKey = null;
            while(lastReadChar != '}') {

                if(lastReadChar != '"') {
                    String suffix = lastKey == null ? "" : " (After key " + lastKey + ")";
                    throw new DecodeException("Found unquoted key while parsing an object!" + suffix);
                }

                try {
                    lastKey = readString(reader);
                } catch (DecodeException ex) {
                    String suffix = lastKey == null ? "" : " (After key " + lastKey + ")";
                    throw new DecodeException("An error occurred while decoding an object key!" + suffix, ex);
                }

                skipWhitespace(reader);
                if(lastReadChar != ':') {
                    throw new DecodeException("Found junk data after key \"" + lastKey + "\"");
                }

                nextReal(reader);
                try {
                    T obj = decodeElement(reader);
                    values.put(lastKey, obj);
                } catch (DecodeException ex) {
                    throw new DecodeException("An error occurred while decoding an object value with key \"" + lastKey + "\"!", ex);
                }

                skipWhitespace(reader);
                if(lastReadChar == ',') {
                    if(nextReal(reader) == '}') {
                        throw new DecodeException("Found unexpected end of object after key " + lastKey + "!");
                    }
                }
            }

            nextReal(reader);
            return context.toMap(values);
        }

        private T decodeList(Reader reader) throws IOException {

            skipWhitespace(reader);
            List<T> values = new ArrayList<>();

            nextReal(reader);
            while (lastReadChar != ']') {

                try {
                    values.add(decodeElement(reader));
                } catch (DecodeException ex) {
                    throw new DecodeException("An error occurred while decoding a list value at index " + values.size(), ex);
                }
                skipWhitespace(reader);

                if (lastReadChar == ',') {
                    if(nextReal(reader) == ']') {
                        throw new DecodeException("Found unexpected end of list after index " + values.size());
                    }
                }
            }

            nextReal(reader);
            return context.toList(values);
        }

        private String readString(Reader reader) throws IOException {

            CharArrayWriter writer = new CharArrayWriter();

            boolean escaped = false;
            while(true) {

                lastReadChar = reader.read();
                if(lastReadChar == -1) {
                    throw new DecodeException("Found EOF while reading a JSON String!");
                }
                if(!escaped && lastReadChar == '"') {
                    break;
                }

                escaped = !escaped && lastReadChar == '\\';
                writer.write(lastReadChar);
            }

            String unescaped = writer.toString();

            StringBuilder output = new StringBuilder();
            int prevIndex = 0;
            int index = 0;
            while((index = unescaped.indexOf('\\', index)) != -1) {

                if(index > prevIndex) {
                    output.append(unescaped, prevIndex, index);
                }

                char next = unescaped.charAt(++index);
                switch (next) {
                    case '\\':
                    case '/':
                    case '"':
                        output.append(next);
                        index++;
                        break;
                    case 'n':
                        output.append('\n');
                        index++;
                        break;
                    case 'r':
                        output.append('\r');
                        index++;
                        break;
                    case 'f':
                        output.append('\f');
                        index++;
                        break;
                    case 'b':
                        output.append('\b');
                        index++;
                        break;
                    case 't':
                        output.append('\t');
                        index++;
                        break;
                    case 'u':

                        char lastRead;
                        do {
                            // Skip all additional u's
                            lastRead = unescaped.charAt(index++);
                        } while (lastRead == 'u');

                        String codePointStr = lastRead + unescaped.substring(index, index + 3);
                        index += 3;

                        try {
                            int codePointValue = Integer.parseUnsignedInt(codePointStr, 16);
                            output.appendCodePoint(codePointValue);

                        } catch (NumberFormatException nfe) {

                            throw new DecodeException("Unable to decode unicode code point: " + codePointStr);
                        }
                        break;

                    default:
                        throw new DecodeException("Invalid escape character " + next + "!");
                }

                prevIndex = index;

            }

            output.append(unescaped.substring(prevIndex));

            nextReal(reader);
            return output.toString();
        }

        private boolean hasInvalidChars(String value, String valid) {
            for(char c : value.toCharArray()) {
                if(valid.indexOf(c) == -1) {
                    return true;
                }
            }
            return false;
        }

    }

}
