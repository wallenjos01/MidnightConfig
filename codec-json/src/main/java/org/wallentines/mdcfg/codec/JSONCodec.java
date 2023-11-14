package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.math.BigDecimal;
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

            if (!context.isList(value)) throw new EncodeException("Not a list: " + value);
            String nextPrefix = prefix + indent;

            Collection<T> collection = context.asList(value);

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

            if (context.isMap(value)) {
                encodeMap(value, prefix, writer);
                return;
            }
            if (context.isList(value)) {
                encodeList(value, prefix, writer);
                return;
            }
            if (context.isString(value)) {
                writer.write("\"" + encodeString(context.asString(value)) + "\"");
                return;
            }
            if (context.isNumber(value)) {
                writer.write(context.asNumber(value).toString());
                return;
            }
            if (context.isBoolean(value)) {
                writer.write(context.asBoolean(value).toString());
                return;
            }
            throw new EncodeException("Unable to serialize " + value + "!");

        }
    }

    private static class Decoder<T> {

        private final SerializeContext<T> context;

        public Decoder(SerializeContext<T> context) {
            this.context = context;
        }

        public T decode(InputStream data, Charset charset) throws IOException {

            BufferedReader reader = new BufferedReader(new InputStreamReader(data, charset));
            T out = decodeElement(reader);
            reader.close();
            return out;
        }

        private void skipWhitespace(BufferedReader reader) throws IOException {
            reader.mark(1);

            int i;
            while((i = reader.read()) <= 32) {
                if(i == -1) {
                    throw new DecodeException("Found EOF while attempting to parse JSON!");
                }

                reader.mark(1);
            }
            reader.reset();
        }

        private T decodeElement(BufferedReader reader) throws IOException {

            skipWhitespace(reader);

            reader.mark(1);
            int c = reader.read();
            if(c == '{') {
                return decodeMap(reader);
            }
            if(c == '[') {
                return decodeList(reader);
            }
            if(c == '"') {
                return decodeString(reader);
            }
            if(c == '}' || c == ']') { // Illegal Characters (in this context)
                throw new DecodeException("Found illegal character " + c);
            }
            reader.reset();
            return decodePrimitive(reader);
        }

        private T decodeString(BufferedReader reader) throws IOException {

            return context.toString(readString(reader));
        }

        private T decodePrimitive(BufferedReader reader) throws IOException {

            String value = readPrimitive(reader);

            if(value.equalsIgnoreCase("true")) {
                return context.toBoolean(true);
            }
            if(value.equalsIgnoreCase("false")) {
                return context.toBoolean(false);
            }
            if(value.equalsIgnoreCase("null")) {
                return null;
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
                    out = new BigDecimal(value);

                }

            } else {
                out = Double.parseDouble(value);
            }

            return context.toNumber(out);
        }

        private T decodeMap(BufferedReader reader) throws IOException {

            skipWhitespace(reader);
            reader.mark(1);
            int c = reader.read();

            Map<String, T> values = new LinkedHashMap<>();

            // Handle empty maps
            if(c == '}') {
                return context.toMap(values);
            }
            reader.reset();

            // A key should come next
            String lastKey = decodeMapEntry(reader, values);
            while((c = reader.read()) == ',') {
                lastKey = decodeMapEntry(reader, values);
            }

            if(c != '}') {
                throw new DecodeException("Found junk data after value with key \"" + lastKey + "\"");
            }

            return context.toMap(values);
        }

        private String decodeMapEntry(BufferedReader reader, Map<String, T> output) throws IOException {

            skipWhitespace(reader);
            int c = reader.read();

            if(c != '"') throw new DecodeException("Found invalid key while reading " + c + "!");

            String key = readString(reader);
            skipWhitespace(reader);

            if(reader.read() != ':') throw new DecodeException("Found junk data after key \"" + key + "\"");

            skipWhitespace(reader);
            T value = decodeElement(reader);
            if(value == null) return key;

            output.put(key, value);
            skipWhitespace(reader);

            return key;
        }

        private T decodeList(BufferedReader reader) throws IOException {

            skipWhitespace(reader);
            reader.mark(1);
            int c = reader.read();
            List<T> values = new ArrayList<>();

            // Handle empty lists
            if(c == ']') {
                return context.toList(values);
            }
            reader.reset();

            T element = decodeElement(reader);
            if(element != null) values.add(element);
            skipWhitespace(reader);

            while((c = reader.read()) == ',') {
                element = decodeElement(reader);
                if(element != null) values.add(element);
                skipWhitespace(reader);
            }

            if(c != ']') throw new DecodeException("Found junk data after list");

            return context.toList(values);
        }

        private String readString(BufferedReader reader) throws IOException {
            StringBuilder output = new StringBuilder();
            boolean escaped = false;
            int c;
            while((c = reader.read()) != '"' || escaped) {

                if(c == -1) {
                    throw new DecodeException("Found EOF while parsing a String!");
                }

                if (escaped) {

                    switch (c) {
                        case '\\':
                        case '/':
                        case '"':
                            output.appendCodePoint(c);
                            break;
                        case 'n':
                            output.append("\n");
                            break;
                        case 'r':
                            output.append("\r");
                            break;
                        case 'f':
                            output.append("\f");
                            break;
                        case 'b':
                            output.append("\b");
                            break;
                        case 't':
                            output.append("\t");
                            break;
                        case 'u':

                            int lastRead;
                            do {
                                // Skip all additional u's
                                lastRead = reader.read();
                            } while (lastRead == 'u');

                            char[] codePoint = new char[4];
                            codePoint[0] = (char) lastRead;
                            if (reader.read(codePoint, 1, 3) != 3) {
                                throw new DecodeException("Not enough data to decode a unicode code point!");
                            }
                            String codePointStr = new String(codePoint);

                            try {
                                int codePointValue = Integer.parseUnsignedInt(codePointStr, 16);
                                output.appendCodePoint(codePointValue);

                            } catch (NumberFormatException nfe) {

                                throw new DecodeException("Unable to decode unicode code point: " + codePointStr);
                            }
                            break;

                        default:
                            throw new DecodeException("Invalid escape character " + c + "!");
                    }

                    escaped = false;

                } else {

                    if (c == '\\') {
                        escaped = true;
                    } else {
                        output.appendCodePoint(c);
                    }
                }
            }


            return output.toString();
        }

        private String readPrimitive(BufferedReader reader) throws IOException {

            StringBuilder output = new StringBuilder();

            int c;
            reader.mark(1);
            while((c = reader.read()) > 32 && c != '}' && c != ',' && c != ']') {
                output.appendCodePoint(c);
                reader.mark(1);
            }

            reader.reset();

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
