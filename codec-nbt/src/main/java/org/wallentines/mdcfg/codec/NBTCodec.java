package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

public class NBTCodec implements Codec {

    private final boolean expectRootName;

    public NBTCodec(boolean expectRootName) {
        this.expectRootName = expectRootName;
    }

    @Override
    public <T> void encode(@NotNull SerializeContext<T> ctx, T t, @NotNull OutputStream os, Charset charset) throws EncodeException, IOException {
        try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os))) {
            new Encoder<>(ctx, dos, expectRootName).encode(t);
        }
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> ctx, @NotNull InputStream is, Charset charset) throws DecodeException, IOException {
        try(DataInputStream dis = new DataInputStream(new BufferedInputStream(is))) {
            return new Decoder<>(ctx, dis, expectRootName).decode();
        }
    }

    private static class Encoder<T> {

        private final SerializeContext<T> ctx;
        private final DataOutput writer;
        private final boolean expectRootName;

        Encoder(SerializeContext<T> ctx, DataOutput writer, boolean expectRootName) {
            this.ctx = ctx;
            this.writer = writer;
            this.expectRootName = expectRootName;
        }

        void encode(T t) throws IOException {

            TagType type = NBTUtil.getTagType(ctx, t);
            if(type == null) {
                throw new EncodeException("Unable to determine NBT type of" + t + "!");
            }

            writer.writeByte(type.getValue());

            if(expectRootName && ctx.isMap(t)) {

                String name = null;
                if(ctx.supportsMeta(t)) {
                    name = ctx.getMetaProperty(t, "nbt.root_name");
                }
                writer.writeUTF(name == null ? "" : name);

            }

            encodeValue(t);
        }

        private void encodeValue(T t) throws IOException {

            TagType type = NBTUtil.getTagType(ctx, t);
            if(type == null) {
                throw new EncodeException("Unable to determine NBT type of" + t + "!");
            }

            switch (type) {
                case BYTE: writer.writeByte(ctx.isBoolean(t)
                        ? ctx.asBoolean(t).getOrThrow(EncodeException::new)
                                ? 0b1
                                : 0b0
                        : ctx.asNumber(t).getOrThrow(EncodeException::new).byteValue()); break;
                case SHORT: writer.writeShort(ctx.asNumber(t).getOrThrow(EncodeException::new).shortValue()); break;
                case INT: writer.writeInt(ctx.asNumber(t).getOrThrow(EncodeException::new).intValue()); break;
                case LONG: writer.writeLong(ctx.asNumber(t).getOrThrow(EncodeException::new).longValue()); break;
                case FLOAT: writer.writeFloat(ctx.asNumber(t).getOrThrow(EncodeException::new).floatValue()); break;
                case DOUBLE: writer.writeDouble(ctx.asNumber(t).getOrThrow(EncodeException::new).doubleValue()); break;
                case STRING: writer.writeUTF(ctx.asString(t).getOrThrow(EncodeException::new)); break;
                case BYTE_ARRAY:
                    if(ctx.isBlob(t)) {
                        ByteBuffer buf = ctx.asBlob(t).getOrThrow(EncodeException::new).asReadOnlyBuffer();
                        buf.rewind();
                        int size = buf.limit();

                        writer.writeInt(size);

                        byte[] copyBuffer = new byte[1024];
                        int remaining = size;
                        while(remaining > 0) {
                            int read = Math.min(remaining, copyBuffer.length);
                            buf.get(copyBuffer, 0, read);
                            remaining -= read;
                            writer.write(copyBuffer, 0, read);
                        }
                        break;
                    }
                case INT_ARRAY:
                case LONG_ARRAY: {

                    Collection<T> list = ctx.asList(t).getOrThrow(EncodeException::new);
                    writer.writeInt(list.size());
                    for (T t1 : list) {
                        encodeValue(t1);
                    }
                    break;
                }
                case LIST: {
                    Collection<T> list = ctx.asList(t).getOrThrow(EncodeException::new);
                    TagType lt = NBTUtil.getListType(ctx, list);
                    if(lt == null) {
                        throw new EncodeException("Unable to determine NBT list type of" + t + "!");
                    }

                    writer.writeByte(lt.getValue());
                    writer.writeInt(list.size());
                    for (T t1 : list) {
                        encodeValue(t1);
                    }
                    break;
                }
                case COMPOUND: {
                    for (String key : ctx.getOrderedKeys(t)) {

                        T value = ctx.get(key, t);
                        TagType tt = NBTUtil.getTagType(ctx, value);
                        if(tt == null) {
                            throw new EncodeException("Unable to determine NBT list type of" + t + "!");
                        }

                        writer.writeByte(tt.getValue());
                        writer.writeUTF(key);
                        encodeValue(value);
                    }
                    writer.writeByte(TagType.END.getValue());
                    break;
                }
            }
        }
    }


    private static class Decoder<T> {

        private final SerializeContext<T> ctx;
        private final DataInput reader;
        private final boolean expectRootName;

        Decoder(SerializeContext<T> ctx, DataInput reader, boolean expectRootName) {
            this.ctx = ctx;
            this.reader = reader;
            this.expectRootName = expectRootName;
        }

        T decode() throws DecodeException, IOException {

            TagType tag = readTagType(reader);
            String rootName = null;
            if(expectRootName) {
                if(tag != TagType.COMPOUND) {
                    throw new DecodeException("Expected root to be a compound!");
                }

                rootName = reader.readUTF();
            }

            T out = decodeValue(tag);

            if(rootName != null && ctx.supportsMeta(out)) {
                ctx.setMetaProperty(out, "nbt.root_name", rootName);
            }

            return out;
        }

        private T decodeValue(TagType type) throws IOException {

            TagType listType = null;

            T out = switch (type) {
                case END -> throw new DecodeException("Found unexpected end tag!");
                case BYTE -> ctx.toNumber(reader.readByte());
                case SHORT -> ctx.toNumber(reader.readShort());
                case INT -> ctx.toNumber(reader.readInt());
                case LONG -> ctx.toNumber(reader.readLong());
                case FLOAT -> ctx.toNumber(reader.readFloat());
                case DOUBLE -> ctx.toNumber(reader.readDouble());
                case STRING -> ctx.toString(reader.readUTF());
                case BYTE_ARRAY -> {

                    int length = reader.readInt();

                    ByteBuffer buffer = ByteBuffer.allocate(length);
                    int remaining = length;
                    byte[] copyBuffer = new byte[1024];

                    try {
                        while (remaining > 0) {
                            int read = Math.min(remaining, copyBuffer.length);
                            reader.readFully(copyBuffer, 0, read);
                            remaining -= read;
                            buffer.put(copyBuffer, 0, read);
                        }
                    } catch (EOFException ex) {
                        throw new DecodeException("Unexpected EOF encountered while reading a blob!", ex);
                    }

                    listType = TagType.BYTE;
                    yield ctx.toBlob(buffer);
                }
                case LIST -> {
                    List<T> list = new ArrayList<>();
                    listType = TagType.byValue(reader.readByte());
                    if(listType == null) {
                        throw new DecodeException("Found unknown tag while reading a list!");
                    }
                    int length = reader.readInt();
                    for(int i = 0 ; i < length ; i++) {
                        list.add(decodeValue(listType));
                    }
                    yield ctx.toList(list);
                }
                case COMPOUND -> {

                    Map<String, T> map = new LinkedHashMap<>();
                    TagType nextTag;
                    while((nextTag = readTagType(reader)) != TagType.END) {
                        map.put(reader.readUTF(), decodeValue(nextTag));
                    }

                    yield ctx.toMap(map);
                }
                case INT_ARRAY -> {
                    List<T> list = new ArrayList<>();
                    int length = reader.readInt();
                    for(int i = 0 ; i < length ; i++) {
                        list.add(ctx.toNumber(reader.readInt()));
                    }
                    listType = TagType.INT;
                    yield ctx.toList(list);
                }
                case LONG_ARRAY -> {

                    List<T> list = new ArrayList<>();
                    int length = reader.readInt();
                    for(int i = 0 ; i < length ; i++) {
                        list.add(ctx.toNumber(reader.readLong()));
                    }
                    listType = TagType.LONG;
                    yield ctx.toList(list);
                }
            };

            if(ctx.supportsMeta(out)) {
                if(listType != null) {
                    ctx.setMetaProperty(out, "nbt.list_type", listType.encode());
                }
                ctx.setMetaProperty(out, "nbt.tag_type", type.encode());
            }

            return out;
        }


        private TagType readTagType(DataInput input) throws IOException {
            int tagValue = input.readByte();
            TagType type = TagType.byValue(tagValue);
            if(type == null) throw new DecodeException("Found unknown tag type " + tagValue + "!");
            return type;
        }

    }
}
