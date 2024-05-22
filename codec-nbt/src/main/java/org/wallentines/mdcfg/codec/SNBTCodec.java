package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A codec for Stringified NBT (SNBT) data
 */
public class SNBTCodec implements Codec {

    private static final Pattern UNQUOTED_KEY_INVALID = Pattern.compile("[\\[\\]\\{\\}\\(\\),\"':]");

    private boolean expectRootName;
    private boolean expectArrayIndices;
    private boolean useDoubleQuotes;

    public SNBTCodec() { }

    /**
     * Creates an SNBT Codec.
     * @param expectRootName Whether a root name should be encoded into and decoded from the SNBT stream.
     */
    public SNBTCodec(boolean expectRootName) {
        this(expectRootName, false);
    }
    /**
     * Creates an SNBT Codec.
     * @param expectRootName Whether a root name should be encoded into and decoded from the SNBT stream.
     * @param expectArrayIndices Whether elements in arrays should be prefixed with their indices
     */
    public SNBTCodec(boolean expectRootName, boolean expectArrayIndices) {
        this.expectRootName = expectRootName;
        this.expectArrayIndices = expectArrayIndices;
    }

    public SNBTCodec expectRootName() {
        this.expectRootName = true;
        return this;
    }

    public SNBTCodec expectArrayIndices() {
        this.expectArrayIndices = true;
        return this;
    }

    public SNBTCodec useDoubleQuotes() {
        this.useDoubleQuotes = true;
        return this;
    }


    @Override
    public <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws EncodeException, IOException {
        try(Encoder<T> enc = new Encoder<>(context, new BufferedWriter(new OutputStreamWriter(stream, charset)))) {
            enc.encode(input);
        } catch (EncodeException | IOException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) throws DecodeException, IOException {

        try(Decoder<T> dec = new Decoder<>(context, new BufferedReader(new InputStreamReader(stream, charset)))) {
            return dec.decode();
        } catch (DecodeException | IOException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class Encoder<T> implements AutoCloseable {

        final SerializeContext<T> context;
        final Writer stream;

        public Encoder(SerializeContext<T> context, Writer stream) {
            this.context = context;
            this.stream = stream;
        }

        private void encode(T input) throws EncodeException, IOException {
            if(expectRootName) {
                String rootName = context.getMetaProperty(input, "nbt.root_name");
                if(rootName == null) throw new EncodeException("Root name is required!");
                encodeKey(rootName);
                stream.write(':');
            }

            encodeValue(input);
        }

        private void encodeValue(T input) throws EncodeException, IOException {
            TagType type = NBTUtil.getTagType(context, input);
            if(type == null) {
                throw new EncodeException("Unable to find tag type for " + input + "!");
            }
            switch (type) {
                case STRING:
                    encodeString(context.asString(input));
                    break;
                case BYTE:
                    stream.write(context.asNumber(input) + "b");
                    break;
                case SHORT:
                    stream.write(context.asNumber(input) + "s");
                    break;
                case INT:
                    stream.write(context.asNumber(input).toString());
                    break;
                case LONG:
                    stream.write(context.asNumber(input) + "L");
                    break;
                case FLOAT:
                    stream.write(context.asNumber(input) + "f");
                    break;
                case DOUBLE:
                    stream.write(context.asNumber(input) + "d");
                    break;
                case BYTE_ARRAY: {
                    stream.write("[B;");
                    if (context.isBlob(input)) {

                        ByteBuffer buf = context.asBlob(input).asReadOnlyBuffer();
                        buf.rewind();
                        int size = buf.limit();
                        for(int i = 0 ; i < size ; i++) {
                            if(i > 0) {
                                stream.write(',');
                            }
                            if(expectArrayIndices) {
                                stream.write(i + ":");
                            }
                            stream.write(Objects.toString(buf.get(i)));
                        }

                    } else {
                        int index = 0;
                        for (T t : context.asList(input)) {
                            if (index > 0) {
                                stream.write(',');
                            }
                            if(expectArrayIndices) {
                                stream.write(index + ":");
                            }
                            index++;
                            stream.write(context.asNumber(t).byteValue() + "b");
                        }
                    }
                    stream.write(']');
                    break;
                }
                case INT_ARRAY: {
                    stream.write("[I;");
                    int index = 0;
                    for (T t : context.asList(input)) {
                        if (index > 0) {
                            stream.write(',');
                        }
                        if(expectArrayIndices) {
                            stream.write(index + ":");
                        }
                        index++;
                        stream.write(Objects.toString(context.asNumber(t).intValue()));
                    }
                    stream.write(']');
                    break;
                }
                case LONG_ARRAY: {
                    stream.write("[L;");
                    int index = 0;
                    for (T t : context.asList(input)) {
                        if (index > 0) {
                            stream.write(',');
                        }
                        if(expectArrayIndices) {
                            stream.write(index + ":");
                        }
                        index++;
                        stream.write(context.asNumber(t).longValue() + "L");
                    }

                    stream.write(']');
                    break;
                }
                case LIST:
                    stream.write("[");
                    int index = 0;
                    for (T t : context.asList(input)) {
                        if (index > 0) {
                            stream.write(',');
                        }
                        if(expectArrayIndices) {
                            stream.write(index + ":");
                        }
                        encodeValue(t);

                        index++;
                    }
                    stream.write(']');
                    break;
                case COMPOUND: {

                    stream.write('{');
                    int i = 0;
                    for (String key : context.getOrderedKeys(input)) {
                        if (i++ > 0) {
                            stream.write(',');
                        }
                        encodeKey(key);
                        stream.write(':');
                        encodeValue(context.get(key, input));

                    }
                    stream.write('}');
                }
            }
        }

        private void encodeString(String string) throws IOException {
            if(!useDoubleQuotes && string.contains("\"")) {
                stream.write("'" + string.replace("'", "\\'") + "'");
            } else {
                stream.write("\"" + string.replace("\"", "\\\"") + "\"");
            }
        }

        private void encodeKey(String key) throws IOException {

            if(key.isEmpty()) {
                stream.write("\"\"");
            } else if(UNQUOTED_KEY_INVALID.matcher(key).find()) {
                stream.write("\"" + key.replace("\"", "\\\"") + "\"");
            } else {
                stream.write(key);
            }

        }

        @Override
        public void close() throws Exception {
            stream.close();
        }
    }


    private class Decoder<T> implements AutoCloseable {

        final SerializeContext<T> context;
        final Reader stream;
        int lastRead;

        Decoder(@NotNull SerializeContext<T> context, @NotNull Reader stream) {
            this.context = context;
            this.stream = stream;
        }

        void readUntil(int chara, ByteArrayOutputStream bos) throws IOException {
            boolean escaped = false;
            while((lastRead = stream.read()) != chara || escaped) {
                if(lastRead == -1) {
                    throw new DecodeException("Found EOF while decoding NBT!");
                }
                escaped = !escaped && lastRead == '\\';
                bos.write(lastRead);
            }
        }

        int nextReal() throws IOException {
            int chara;
            while(Character.isWhitespace((chara = stream.read()))) {
                if(lastRead == -1) {
                    return -1;
                }
            }
            lastRead = chara;
            return lastRead;
        }

        T decode() throws DecodeException, IOException {
            nextReal();

            String rootName = null;
            if(expectRootName) {
                rootName = decodeKey();
                nextReal();
            }
            T out = decodeElement();

            if(expectRootName) {
                context.setMetaProperty(out, "nbt.root_name", rootName);
            }
            return out;
        }

        String decodeKey() throws IOException{

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if(lastRead == '\'' || lastRead == '"') {
                readUntil(lastRead, bos);
                if(nextReal() != ':') {
                    throw new DecodeException("Found invalid character " + lastRead + " after reading a key!");
                }
                return bos.toString();
            } else {

                bos.write(lastRead);
                readUntil(':', bos);
                String key = bos.toString();

                Matcher matcher = UNQUOTED_KEY_INVALID.matcher(key);
                if(matcher.find()) {
                    throw new DecodeException("Found invalid character in a key! Current key: " + key);
                }
                return key;
            }
        }

        T decodeCompound() throws IOException {

            Map<String, T> values = new LinkedHashMap<>();

            if(nextReal() != '}') {

                String key = decodeKey();

                nextReal();
                T value = decodeElement();
                values.put(key, value);

                while (lastRead == ',') {

                    if(nextReal() == '}') {
                        break;
                    }
                    key = decodeKey();

                    nextReal();
                    value = decodeElement();
                    values.put(key, value);
                }
            }
            if(lastRead != '}') {
                throw new DecodeException("Found invalid character after reading compound! Keys: " + Arrays.toString(values.keySet().toArray(new String[0])));
            }
            nextReal();

            T out = context.toMap(values);
            NBTUtil.setTagType(context, out, TagType.COMPOUND);
            return out;
        }

        T decodeString() throws IOException {

            int quoteChar = lastRead;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            readUntil(quoteChar, bos);

            String unescaped = bos.toString();

            StringBuilder output = new StringBuilder();
            int prevIndex = 0;
            int index = 0;
            while((index = unescaped.indexOf('\\', index)) != -1) {

                if (index > prevIndex) {
                    output.append(unescaped, prevIndex, index);
                }

                char next = unescaped.charAt(++index);
                if(next == quoteChar) {
                    output.appendCodePoint(quoteChar);
                    index++;
                } else {
                    switch (next) {
                        case '\\':
                        case '/':
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
                }

                prevIndex = index;
            }
            output.append(unescaped.substring(prevIndex));
            nextReal();

            return context.toString(output.toString());
        }

        private T decodeNumber() throws IOException {

            StringBuilder output = new StringBuilder();

            do {
                output.append((char) lastRead);
            }
            while((lastRead = stream.read()) > ' '
                    && lastRead != '}'
                    && lastRead != ','
                    && lastRead != ']');

            String value = output.toString();

            char suffix = value.charAt(value.length() - 1);
            TagType type;
            Number num;

            try {
                if (value.indexOf('.') == -1) {
                    switch (suffix) {
                        case 'B':
                        case 'b':
                            num = Byte.parseByte(value.substring(0, value.length() - 1));
                            type = TagType.BYTE;
                            break;
                        case 'S':
                        case 's':
                            num = Short.parseShort(value.substring(0, value.length() - 1));
                            type = TagType.SHORT;
                            break;
                        case 'L':
                        case 'l':
                            num = Long.parseLong(value.substring(0, value.length() - 1));
                            type = TagType.LONG;
                            break;
                        default:
                            num = Integer.parseInt(value);
                            type = TagType.INT;
                    }
                } else {
                    switch (suffix) {
                        case 'F':
                        case 'f':
                            num = Float.parseFloat(value.substring(0, value.length() - 1));
                            type = TagType.FLOAT;
                            break;
                        case 'D':
                        case 'd':
                            num = Double.parseDouble(value.substring(0, value.length() - 1));
                            type = TagType.DOUBLE;
                            break;
                        default:
                            num = Double.parseDouble(value);
                            type = TagType.DOUBLE;
                    }
                }
            } catch (NumberFormatException ex) {
                throw new DecodeException("Unable to parse " + value + " as a number!");
            }

            T out = context.toNumber(num);
            NBTUtil.setTagType(context, out, type);
            return out;
        }

        private T decodeList() throws IOException {

            TagType listType = TagType.LIST;
            switch (nextReal()) {
                case 'I':
                    listType = TagType.INT_ARRAY;
                    break;
                case 'B':
                    listType = TagType.BYTE_ARRAY;
                    break;
                case 'L':
                    listType = TagType.LONG_ARRAY;
                    break;
                case ']':
                    T out = context.toList(new ArrayList<>());
                    NBTUtil.setTagType(context, out, TagType.LIST);
                    nextReal();
                    return out;
            }
            if(listType != TagType.LIST) {
                if(nextReal() != ';') {
                    throw new DecodeException("Found invalid characters at the beginning of a list!");
                }
                nextReal();
            }

            List<T> values = new ArrayList<>();
            do {
                if(!values.isEmpty() && nextReal() == ']') {
                    break;
                }
                try {
                    if(expectArrayIndices) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bos.write(lastRead);
                        readUntil(':', bos);
                        String num = bos.toString();
                        int index = Integer.parseInt(num);
                        if(index != values.size()) {
                            throw new DecodeException("Found out-of-order element with index " + index + "!");
                        }
                        nextReal();
                    }
                    values.add(decodeElement());
                } catch (DecodeException ex) {
                    throw new DecodeException("An error occurred while decoding a list value at index " + values.size(), ex);
                }

            } while (lastRead == ',');

            if(lastRead != ']') {
                throw new DecodeException("Found invalid character after reading list!");
            }
            nextReal();

            T out;
            if(listType == TagType.BYTE_ARRAY) {
                ByteBuffer buf = ByteBuffer.allocate(values.size());
                for(T t : values) {
                    buf.put(context.asNumber(t).byteValue());
                }
                out = context.toBlob(buf);
            } else {
                out = context.toList(values);
            }
            NBTUtil.setTagType(context, out, listType);
            return out;
        }

        T decodeElement() throws IOException {
            switch (lastRead) {
                case '\'':
                case '"':
                    return decodeString();
                case '[':
                    return decodeList();
                case '{':
                    return decodeCompound();
                default:
                    return decodeNumber();
            }
        }

        @Override
        public void close() throws Exception {
            stream.close();
        }
    }
}
