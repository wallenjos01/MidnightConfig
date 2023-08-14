package org.wallentines.mdcfg.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
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

    private static final Logger LOGGER = LoggerFactory.getLogger("BinaryCodec");

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
    public <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset) {

        try {
            stream.write(HEADER.getBytes(StandardCharsets.US_ASCII));
            stream.write(compression.index());
        } catch (IOException ex) {
            LOGGER.error("An exception occurred while writing config binary to a stream!", ex);
        }

        try(DataOutputStream dos = compression.createOutputStream(stream)) {

            encodeValue(context, input, dos);

        } catch (IOException ex) {

            LOGGER.error("An exception occurred while writing config binary to a stream!", ex);
        }

    }

    private <T> void encodeValue(SerializeContext<T> context, T input, DataOutputStream dos) throws IOException {

        if (context.isNumber(input)) {

            encodeNumber(context.asNumber(input), dos);

        } else if(context.isString(input)) {

            dos.writeByte(Type.STRING.index());
            writeString(context.asString(input), dos);

        } else if(context.isBoolean(input)) {

            Boolean value = context.asBoolean(input);
            dos.writeByte(Type.BOOLEAN.index());
            dos.writeBoolean(value);

        } else if(context.isList(input)) {

            Collection<T> objects = context.asList(input);
            dos.writeByte(Type.LIST.index());
            dos.writeInt(objects.size());

            for(T o : objects) {
                encodeValue(context, o, dos);
            }

        } else if(context.isMap(input)) {

            Map<String, T> entries = context.asOrderedMap(input);
            dos.writeByte(Type.SECTION.index());
            dos.writeInt(entries.size());

            for(String s : entries.keySet()) {
                writeString(s, dos);
                encodeValue(context, entries.get(s), dos);
            }
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

        } else {

            double floatingValue = number.doubleValue();
            long decimalValue = number.longValue();

            if(Math.abs(floatingValue - decimalValue) > 0.0000001) {
                stream.writeDouble(floatingValue);
            } else {
                stream.writeLong(number.longValue());
            }
        }
    }

    private void writeString(String str, DataOutputStream stream) throws IOException {

        stream.writeInt(str.length());
        stream.write(str.getBytes(StandardCharsets.UTF_8));
    }



    @Override
    public <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset) throws DecodeException {

        Compression compression;
        try {
            String header = new String(stream.readNBytes(HEADER.length()), StandardCharsets.US_ASCII);
            if(!header.equals(HEADER)) {
                throw new DecodeException("Unable to decode config binary! Missing or invalid header!");
            }

            compression = Compression.byIndex(stream.read());
            if(compression == null) {
                throw new DecodeException("Unable to decode config binary! Unknown compression type!");
            }

        } catch (IOException ex) {
            throw new DecodeException("An exception occurred while writing config binary to a stream! " + ex.getMessage());
        }

        try(DataInputStream dis = compression.createInputStream(stream)) {

            T out = decodeValue(context, dis);
            if(out == null) {
                throw new DecodeException("Unexpected none type found while decoding config binary!");
            }

            return out;

        } catch (IOException ex) {

            throw new DecodeException("Unable to decode config binary! IOException: " + ex.getMessage());
        }
    }

    private <T> T decodeValue(SerializeContext<T> context, DataInputStream stream) throws IOException {

        int typeIndex = stream.readByte();
        Type t = Type.byIndex(typeIndex);

        if(t == null) {
            throw new DecodeException("Found invalid type " + typeIndex + "!");
        }

        switch (t) {
            case NONE:
                return null;

            case INTEGER:
            case LONG:
            case SHORT:
            case BYTE:
            case FLOAT:
            case DOUBLE:
                return context.toNumber(decodeNumber(t, stream));

            case STRING:
                return context.toString(readString(stream));

            case BOOLEAN:
                return context.toBoolean(stream.readBoolean());

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
        }

        throw new DecodeException("Invalid number type!");
    }

    private String readString(DataInputStream stream) throws IOException{

        int length = stream.readInt();
        byte[] data = stream.readNBytes(length);

        return new String(data, StandardCharsets.UTF_8);
    }



    private enum Type {
        NONE,
        INTEGER,
        LONG,
        SHORT,
        BYTE,
        FLOAT,
        DOUBLE,
        STRING,
        BOOLEAN,
        LIST,
        SECTION;

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
