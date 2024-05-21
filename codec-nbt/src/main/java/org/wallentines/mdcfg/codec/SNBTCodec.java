package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

public class SNBTCodec implements Codec {

    public static final SNBTCodec INSTANCE = new SNBTCodec(false);

    private final boolean expectRootName;

    public SNBTCodec(boolean expectRootName) {
        this.expectRootName = expectRootName;
    }

    @Override
    public <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws EncodeException, IOException {
        try(Encoder<T> enc = new Encoder<>(context, new BufferedWriter(new OutputStreamWriter(stream, charset)), expectRootName)) {
            enc.encode(input);
        } catch (EncodeException | IOException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) throws DecodeException, IOException {

        try(Decoder<T> dec = new Decoder<>(context, new BufferedReader(new InputStreamReader(stream, charset)), expectRootName)) {
            return dec.decode();
        } catch (DecodeException | IOException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class Encoder<T> implements AutoCloseable {

        final SerializeContext<T> context;
        final Writer stream;
        final boolean expectRootName;

        public Encoder(SerializeContext<T> context, Writer stream, boolean expectRootName) {
            this.context = context;
            this.stream = stream;
            this.expectRootName = expectRootName;
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
                    stream.write("\"" + context.asString(input).replace("\"", "\\\"") + "\"");
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
                    stream.write(context.asNumber(input) + "l");
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
                            stream.write(Objects.toString(buf.get(i)));
                        }

                    } else {
                        int index = 0;
                        for (T t : context.asList(input)) {
                            if (index++ > 0) {
                                stream.write(',');
                            }
                            stream.write(Objects.toString(context.asNumber(t).longValue()));
                        }
                    }
                    stream.write(']');
                    break;
                }
                case INT_ARRAY: {
                    stream.write("[I;");
                    int index = 0;
                    for (T t : context.asList(input)) {
                        if (index++ > 0) {
                            stream.write(',');
                        }
                        stream.write(Objects.toString(context.asNumber(t).intValue()));
                    }
                    stream.write(']');
                    break;
                }
                case LONG_ARRAY: {
                    stream.write("[L;");
                    int index = 0;
                    for (T t : context.asList(input)) {
                        if (index++ > 0) {
                            stream.write(',');
                        }
                        stream.write(Objects.toString(context.asNumber(t).longValue()));
                    }

                    stream.write(']');
                    break;
                }
                case LIST:
                    stream.write("[");
                    int index = 0;
                    for (T t : context.asList(input)) {
                        if (index++ > 0) {
                            stream.write(',');
                        }
                        encodeValue(t);
                    }
                    stream.write(']');
                    break;
                case COMPOUND: {

                    Map<String, T> values = context.asMap(input);
                    stream.write('{');
                    int i = 0;
                    for (Map.Entry<String, T> ent : values.entrySet()) {
                        if (i++ > 0) {
                            stream.write(',');
                        }
                        encodeKey(ent.getKey());
                        stream.write(':');
                        encodeValue(ent.getValue());

                    }
                    stream.write('}');
                }
            }
        }

        private void encodeKey(String key) throws IOException {
            stream.write("\"" + key.replace("\"", "\\\"") + "\"");
        }

        @Override
        public void close() throws Exception {
            stream.close();
        }
    }


    private static class Decoder<T> implements AutoCloseable {

        final SerializeContext<T> context;
        final Reader stream;
        final boolean expectRootName;
        int lastRead;

        Decoder(@NotNull SerializeContext<T> context, @NotNull Reader stream, boolean expectRootName) {
            this.context = context;
            this.stream = stream;
            this.expectRootName = expectRootName;
        }

        void readUntil(int chara, ByteArrayOutputStream bos) throws IOException {
            boolean escaped = false;
            while((lastRead = stream.read()) != chara) {
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

            if(lastRead != '{') {
                throw new DecodeException("Expected a compound!");
            }
            T out = decodeCompound();

            if(expectRootName) {
                context.setMetaProperty(out, "nbt.root_name", rootName);
            }
            return out;
        }

        String decodeKey() throws IOException{

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if(lastRead == '\'' || lastRead == '"') {
                readUntil(lastRead, bos);
                nextReal();
                if(lastRead != ':') {
                    throw new DecodeException("Found invalid character " + lastRead + " after reading a key!");
                }
            } else {
                do {
                    if(lastRead == -1) {
                        throw new DecodeException("Found EOF while decoding an NBT key!");
                    }
                    bos.write(lastRead);
                    if(Character.isWhitespace(lastRead) || lastRead == '\'' || lastRead == '"') {
                        throw new DecodeException("Found invalid character " + lastRead + " in a key! Current key: " + bos.toString());
                    }
                } while((lastRead = stream.read()) != ':');
                nextReal();
            }

            return bos.toString().trim();
        }

        T decodeCompound() throws IOException {

            Map<String, T> values = new HashMap<>();
            do {
                nextReal();
                String key = decodeKey();

                nextReal();
                T value = decodeElement();
                values.put(key, value);

            } while(lastRead == ',');

            if(lastRead != '}') {
                throw new DecodeException("Found invalid character after reading compound!");
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
                    output.append(quoteChar);
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
            }
            output.append(unescaped.substring(prevIndex));
            nextReal();

            return context.toString(output.toString());
        }

        private T decodePrimitive() throws IOException {

            StringBuilder output = new StringBuilder();

            do {
                output.append((char) lastRead);
            }
            while((lastRead = stream.read()) > ' '
                    && lastRead != '}'
                    && lastRead != ','
                    && lastRead != ']');

            String value = output.toString();

            if(value.equalsIgnoreCase("true")) {
                T out = context.toBoolean(true);
                NBTUtil.setTagType(context, out, TagType.BYTE);
                return out;
            }
            if(value.equalsIgnoreCase("false")) {
                T out = context.toBoolean(false);
                NBTUtil.setTagType(context, out, TagType.BYTE);
                return out;
            }

            char suffix = value.charAt(value.length() - 1);
            TagType type;
            Number num;

            try {
                if (value.indexOf('.') == -1) {
                    switch (suffix) {
                        case 'b':
                            num = Byte.parseByte(value.substring(0, value.length() - 1));
                            type = TagType.BYTE;
                            break;
                        case 's':
                            num = Short.parseShort(value.substring(0, value.length() - 1));
                            type = TagType.SHORT;
                            break;
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
                        case 'f':
                            num = Float.parseFloat(value.substring(0, value.length() - 1));
                            type = TagType.FLOAT;
                            break;
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

            nextReal();
            TagType listType = TagType.LIST;
            switch (lastRead) {
                case 'I':
                    listType = TagType.INT_ARRAY;
                    break;
                case 'B':
                    listType = TagType.BYTE_ARRAY;
                    break;
                case 'L':
                    listType = TagType.LONG_ARRAY;
            }
            if(listType != TagType.LIST) {
                nextReal();
                if(lastRead != ';') {
                    throw new DecodeException("Found invalid characters at the beginning of a list!");
                }
                nextReal();
            }

            List<T> values = new ArrayList<>();
            do {
                if(!values.isEmpty()) nextReal();
                try {
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
                    return decodePrimitive();
            }
        }

        @Override
        public void close() throws Exception {
            stream.close();
        }
    }
}
