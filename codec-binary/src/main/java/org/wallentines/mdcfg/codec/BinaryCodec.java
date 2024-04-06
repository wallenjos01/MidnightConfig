package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;


/**
 * A codec for storing configuration data as an optionally-compressed binary which is small/quick to decode, but not
 * meant to be easily human-editable
 */
public class BinaryCodec implements Codec {

    private static final String HEADER = "MDCB";

    private final Compression compression;

    /**
     * Constructs a new codec instance with the given compression scheme.
     * @param compression The compression scheme
     */
    public BinaryCodec(Compression compression) {
        this.compression = compression;
    }

    /**
     * Creates a new file codec using the ZSTD compression scheme. Note that zstd-jni will need to be in the classpath
     * @return A new file codec
     */
    public static FileCodec fileCodec() {
        return fileCodec(new BinaryCodec(Compression.ZSTD));
    }

    /**
     * Creates a new file codec using the given binary codec
     * @return A new file codec
     */
    public static FileCodec fileCodec(BinaryCodec codec) {
        return new FileCodec(codec, "mdb");
    }

    @Override
    public <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws IOException, EncodeException {

        stream.write(HEADER.getBytes(StandardCharsets.US_ASCII));
        stream.write(compression.index());

        try(DataOutputStream dos = compression.createOutputStream(stream)) {
            encodeValue(context, input, dos);
        }

    }

    private <T> void encodeValue(SerializeContext<T> context, T input, DataOutputStream dos) throws IOException {

        switch (context.getType(input)) {
            case STRING:
                dos.writeByte(Type.STRING.index());
                writeString(context.asString(input), dos);
                break;

            case NUMBER:
                encodeNumber(context.asNumber(input), dos);
                break;

            case BOOLEAN:
                Boolean value = context.asBoolean(input);
                dos.writeByte(Type.BOOLEAN.index());
                dos.writeBoolean(value);
                break;

            case BLOB:
                ByteBuffer buffer = context.asBlob(input);
                dos.writeByte(Type.BLOB.index());
                dos.write(buffer.capacity());
                dos.write(buffer.array());
                break;

            case LIST:
                Collection<T> objects = context.asList(input);
                dos.writeByte(Type.LIST.index());
                dos.writeInt(objects.size());

                for(T o : objects) {
                    encodeValue(context, o, dos);
                }
                break;

            case MAP:
                Map<String, T> entries = context.asOrderedMap(input);
                dos.writeByte(Type.SECTION.index());
                dos.writeInt(entries.size());

                for(String s : entries.keySet()) {
                    writeString(s, dos);
                    encodeValue(context, entries.get(s), dos);
                }
                break;

            case NULL:
                dos.writeByte(Type.NONE.index());

            default:
                throw new EncodeException("Don't know how to encode " + input + "!");
        }
    }

    private void encodeNumber(Number number, DataOutputStream stream) throws IOException {

        if(number instanceof Integer) {

            stream.writeByte(Type.INTEGER.index());
            stream.writeInt(number.intValue());

        } else if(number instanceof Long) {

            stream.writeByte(Type.LONG.index());
            stream.writeLong(number.longValue());

        } else if(number instanceof Short) {

            stream.writeByte(Type.SHORT.index());
            stream.writeShort(number.shortValue());

        } else if(number instanceof Byte) {

            stream.writeByte(Type.BYTE.index());
            stream.writeByte(number.shortValue());

        } else if(number instanceof Float) {

            stream.writeByte(Type.FLOAT.index());
            stream.writeFloat(number.floatValue());

        } else if(number instanceof Double) {

            stream.writeByte(Type.DOUBLE.index());
            stream.writeDouble(number.doubleValue());

        } else if(number instanceof BigDecimal) {

            stream.writeByte(Type.BIG_DECIMAL.index());
            writeString(number.toString(), stream);

        } else {

            if(ConfigPrimitive.isInteger(number)) {
                stream.writeLong(number.longValue());
            } else {
                stream.writeDouble(number.doubleValue());
            }
        }
    }

    private void writeString(String str, DataOutputStream stream) throws IOException {

        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        stream.writeInt(data.length);
        stream.write(data);
    }


    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) throws DecodeException, IOException {

        return new Decoder<>(context).decode(stream);
    }

    private static class Decoder<T> {

        private final SerializeContext<T> context;
        private final byte[] copyBuffer = new byte[1024];

        public Decoder(SerializeContext<T> context) {
            this.context = context;
        }

        private T decode(InputStream stream) throws DecodeException, IOException {

            byte[] headerBytes = new byte[HEADER.length()];
            if(stream.read(headerBytes) != headerBytes.length || !new String(headerBytes, StandardCharsets.US_ASCII).equals(HEADER)) {
                throw new DecodeException("Unable to decode config binary! Missing or invalid header!");
            }

            Compression compression = Compression.byIndex(stream.read());
            if(compression == null) {
                throw new DecodeException("Unable to decode config binary! Unknown compression type!");
            }

            try(DataInputStream dis = compression.createInputStream(stream)) {
                return decodeValue(context, dis);
            }
        }

        private T decodeValue(SerializeContext<T> context, DataInputStream stream) throws IOException {

            int typeIndex = stream.readByte();
            Type t = Type.byIndex(typeIndex);

            if(t == null) {
                throw new DecodeException("Found invalid type " + typeIndex + "!");
            }

            switch (t) {
                case NONE:
                    return context.nullValue();

                case INTEGER:
                case LONG:
                case SHORT:
                case BYTE:
                case FLOAT:
                case DOUBLE:
                case BIG_DECIMAL:
                    return context.toNumber(decodeNumber(t, stream));

                case STRING:
                    return context.toString(readString(stream));

                case BOOLEAN:
                    return context.toBoolean(stream.readBoolean());

                case BLOB:
                    int bytes = stream.readInt();
                    if(bytes < 1) {
                        throw new DecodeException("Unable to decode empty blob!");
                    }

                    ByteBuffer buf = ByteBuffer.allocate(bytes);
                    int remaining = bytes;
                    while(remaining > 0) {

                        int read = stream.read(copyBuffer, 0, Math.min(remaining, copyBuffer.length));
                        if(read <= 0) {
                            throw new DecodeException("Unexpected EOF encountered while reading a blob!");
                        }
                        remaining -= read;
                        buf.put(copyBuffer, 0, read);
                    }
                    return context.toBlob(buf);

                case LIST: {

                    int length = stream.readInt();
                    List<T> out = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        out.add(decodeValue(context, stream));
                    }
                    return context.toList(out);
                }
                case SECTION: {

                    int length = stream.readInt();
                    Map<String, T> out = new LinkedHashMap<>();
                    for (int i = 0; i < length; i++) {
                        out.put(readString(stream), decodeValue(context, stream));
                    }
                    return context.toMap(out);
                }
            }

            throw new DecodeException("Don't know how to decode type " + t.name() + "!");
        }

        private Number decodeNumber(Type type, DataInputStream stream) throws IOException {

            switch (type) {
                case INTEGER:
                    return stream.readInt();
                case LONG:
                    return stream.readLong();
                case SHORT:
                    return stream.readShort();
                case BYTE:
                    return stream.readByte();
                case FLOAT:
                    return stream.readFloat();
                case DOUBLE:
                    return stream.readDouble();
                case BIG_DECIMAL:
                    return new BigDecimal(readString(stream));
            }

            throw new DecodeException("Invalid number type!");
        }

        private String readString(DataInputStream stream) throws IOException {

            int length = stream.readInt();
            if(length == 0) {
                return "";
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int remaining = length;
            while(remaining > 0) {

                int bytesRead = stream.read(copyBuffer, 0, Math.min(remaining, copyBuffer.length));
                if(bytesRead <= 0) {
                    throw new DecodeException("Unexpected EOF encountered while reading a String!");
                }

                remaining -= bytesRead;
                bos.write(copyBuffer, 0, bytesRead);
            }

            return bos.toString(StandardCharsets.UTF_8);
        }
    }


    private enum Type {
        NONE,
        INTEGER,
        LONG,
        SHORT,
        BYTE,
        FLOAT,
        DOUBLE,
        BIG_DECIMAL,
        STRING,
        BOOLEAN,
        LIST,
        SECTION,
        BLOB;

        int index() {
            return ordinal();
        }

        static Type byIndex(int index) {
            if(index < 0 || index >= values().length) {
                return null;
            }
            return values()[index];
        }
    }

    public enum Compression {
        NONE,
        DEFLATE,
        ZSTD;

        int index() {
            return ordinal();
        }

        DataOutputStream createOutputStream(OutputStream os) throws IOException {

            switch (this) {
                case NONE:
                    return new DataOutputStream(os);
                case DEFLATE:
                    return new DataOutputStream(new DeflaterOutputStream(os));
                case ZSTD:
                    // Do not import this class, so other compression schemes can be used if zstd-jni is not in the classpath
                    return new DataOutputStream(new com.github.luben.zstd.ZstdOutputStream(os));
            }

            throw new IllegalStateException("Unknown compression type");
        }

        DataInputStream createInputStream(InputStream is) throws IOException {

            switch (this) {
                case NONE:
                    return new DataInputStream(is);
                case DEFLATE:
                    return new DataInputStream(new InflaterInputStream(is));
                case ZSTD:
                    // Do not import this class, so other compression schemes can be used if zstd-jni is not in the classpath
                    return new DataInputStream(new com.github.luben.zstd.ZstdInputStream(is));
            }

            throw new IllegalStateException("Unknown compression type");
        }

        static Compression byIndex(int index) {
            if(index < 0 || index >= values().length) {
                return null;
            }
            return values()[index];
        }

    }


}
