package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

public class JSONCodec implements Codec {

    private final boolean shouldIndent;
    private final String indent;

    public JSONCodec() {
        this(0);
    }
    public JSONCodec(int indentAmount) {
        this.indent = indentAmount <= 0 ? "" : " ".repeat(indentAmount);
        this.shouldIndent = indentAmount > 0;
    }

    public static JSONCodec minified() {
        return new JSONCodec(0);
    }

    public static JSONCodec readable() {
        return new JSONCodec(4);
    }

    public static FileCodec fileCodec() {
        return new FileCodec(readable(), "json");
    }

    public static ConfigObject loadConfig(String string) {
        return minified().decode(ConfigContext.INSTANCE, string);
    }

    public static ConfigObject loadConfig(InputStream stream) {
        return minified().decode(ConfigContext.INSTANCE, stream);
    }

    @Override
    public <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset) {
        return new Decoder<>(context).decode(stream, charset);
    }

    @Override
    public <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset) {
        new Encoder<>(context).encode(input, stream, charset);
    }

    private class Encoder<T> {
        private final SerializeContext<T> context;

        public Encoder(SerializeContext<T> context) {
            this.context = context;
        }

        public void encode(T section, OutputStream stream, Charset charset) {

            try {

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
                encode(section, "", writer);
                writer.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void encodeMap(T section, String prefix, BufferedWriter writer) throws IOException {

            if (!context.isMap(section)) throw new IllegalArgumentException("Not a map: " + section);

            Collection<String> keys = context.getOrderedKeys(section);
            String nextPrefix = prefix + indent;

            writer.write("{");
            if(keys.size() == 0) {
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
                writer.write(nextPrefix + "\"" + key + "\":");
                if(shouldIndent) writer.write(" ");
                encode(value, nextPrefix, writer);
            }
            if(shouldIndent) writer.write("\n");
            writer.write(prefix + "}");
        }

        private void encodeList(T value, String prefix, BufferedWriter writer) throws IOException {

            if (!context.isList(value)) throw new IllegalArgumentException("Not a list: " + value);
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
                writer.write("\"" + context.asString(value).replace("\"", "\\\"") + "\"");
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
            throw new IllegalStateException("Unable to serialize " + value + "!");

        }
    }

    private static class Decoder<T> {

        private final SerializeContext<T> context;

        public Decoder(SerializeContext<T> context) {
            this.context = context;
        }

        public T decode(InputStream data, Charset charset) {

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(data, charset));
                T out = decodeElement(reader);
                reader.close();
                return out;

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
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

        private T decodeElement(BufferedReader reader) throws IOException{

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

            if(c != '}') throw new DecodeException("Found junk data after value with key \"" + lastKey + "\"");

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

                if(c == '\\') {
                    if(escaped) {
                        output.appendCodePoint(c);
                    } else {
                        escaped = true;
                    }
                } else {

                    if(escaped && c != '"') {
                        output.append('\\');
                    }

                    output.appendCodePoint(c);
                    escaped = false;
                }
            }
            return output.toString();
        }

        private String readPrimitive(BufferedReader reader) throws IOException {

            StringBuilder output = new StringBuilder();
            reader.mark(64);

            char[] buffer = new char[64];
            int read;
            while((read = reader.read(buffer)) > 0) {

                int i;
                for(i = 0 ; i < read ; i++) {
                    char c = buffer[i];
                    if(c <= 32 || c == ',' || c == '{' || c == '}' || c == '[' || c == ']') break;
                }

                int valueLength = i;
                output.append(buffer, 0, valueLength);

                if(valueLength == 64) {
                    reader.mark(64);
                } else {
                    reader.reset();
                    if(reader.skip(valueLength) != valueLength) throw new DecodeException("An unexpected error occurred while parsing JSON!");
                    break;
                }
            }

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
