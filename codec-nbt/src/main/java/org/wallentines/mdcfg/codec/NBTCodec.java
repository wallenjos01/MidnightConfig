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

        DataOutput dos = new DataOutputStream(os);

        TagType type = NBTUtil.getTagType(ctx, t);
        if(type == null) {
            throw new EncodeException("Unable to determine NBT type of" + t + "!");
        }

        dos.writeByte(type.getValue());

        if(expectRootName && ctx.isMap(t)) {

            String name = null;
            if(ctx.supportsMeta(t)) {
                name = ctx.getMetaProperty(t, "nbt.root_name");
            }
            dos.writeUTF(name == null ? "" : name);

        }

        encode(ctx, t, dos);
    }

    private <T> void encode(SerializeContext<T> ctx, T t, DataOutput dos) throws IOException {

        TagType type = NBTUtil.getTagType(ctx, t);
        if(type == null) {
            throw new EncodeException("Unable to determine NBT type of" + t + "!");
        }

        switch (type) {
            case BYTE: dos.writeByte(ctx.isBoolean(t) ? ctx.asBoolean(t) ? 0b1 : 0b0 : ctx.asNumber(t).byteValue()); break;
            case SHORT: dos.writeShort(ctx.asNumber(t).shortValue()); break;
            case INT: dos.writeInt(ctx.asNumber(t).intValue()); break;
            case LONG: dos.writeLong(ctx.asNumber(t).longValue()); break;
            case FLOAT: dos.writeFloat(ctx.asNumber(t).floatValue()); break;
            case DOUBLE: dos.writeDouble(ctx.asNumber(t).doubleValue()); break;
            case STRING: dos.writeUTF(ctx.asString(t)); break;
            case BYTE_ARRAY:
                if(ctx.isBlob(t)) {
                    ByteBuffer buf = ctx.asBlob(t).asReadOnlyBuffer();
                    buf.rewind();
                    int size = buf.limit();

                    dos.writeInt(size);

                    byte[] copyBuffer = new byte[1024];
                    int remaining = size;
                    while(remaining > 0) {
                        int read = Math.min(remaining, copyBuffer.length);
                        buf.get(copyBuffer, 0, read);
                        remaining -= read;
                        dos.write(copyBuffer, 0, read);
                    }
                    break;
                }
            case INT_ARRAY:
            case LONG_ARRAY: {

                Collection<T> list = ctx.asList(t);
                dos.writeInt(list.size());
                for (T t1 : list) {
                    encode(ctx, t1, dos);
                }
                break;
            }
            case LIST: {
                Collection<T> list = ctx.asList(t);
                TagType lt = NBTUtil.getListType(ctx, list);
                if(lt == null) {
                    throw new EncodeException("Unable to determine NBT list type of" + t + "!");
                }

                dos.writeByte(lt.getValue());
                dos.writeInt(list.size());
                for (T t1 : list) {
                    encode(ctx, t1, dos);
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

                    dos.writeByte(tt.getValue());
                    dos.writeUTF(key);
                    encode(ctx, value, dos);
                }
                dos.writeByte(TagType.END.getValue());
                break;
            }
        }
    }


    @Override
    public <T> T decode(@NotNull SerializeContext<T> ctx, @NotNull InputStream is, Charset charset) throws DecodeException, IOException {

        DataInput dis = new DataInputStream(is);

        TagType tag = readTagType(dis);
        String rootName = null;
        if(expectRootName) {
            if(tag != TagType.COMPOUND) {
                throw new DecodeException("Expected root to be a compound!");
            }

            rootName = dis.readUTF();
        }

        T out = decode(ctx, dis, tag);

        if(rootName != null && ctx.supportsMeta(out)) {
            ctx.setMetaProperty(out, "nbt.root_name", rootName);
        }

        return out;
    }

    private <T> T decode(@NotNull SerializeContext<T> ctx, @NotNull DataInput dis, TagType type) throws IOException {

        TagType listType = null;

        T out = switch (type) {
            case END -> throw new DecodeException("Found unexpected end tag!");
            case BYTE -> ctx.toNumber(dis.readByte());
            case SHORT -> ctx.toNumber(dis.readShort());
            case INT -> ctx.toNumber(dis.readInt());
            case LONG -> ctx.toNumber(dis.readLong());
            case FLOAT -> ctx.toNumber(dis.readFloat());
            case DOUBLE -> ctx.toNumber(dis.readDouble());
            case STRING -> ctx.toString(dis.readUTF());
            case BYTE_ARRAY -> {

                int length = dis.readInt();

                ByteBuffer buffer = ByteBuffer.allocate(length);
                int remaining = length;
                byte[] copyBuffer = new byte[1024];

                try {
                    while (remaining > 0) {
                        int read = Math.min(remaining, copyBuffer.length);
                        dis.readFully(copyBuffer, 0, read);
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
                listType = TagType.byValue(dis.readByte());
                if(listType == null) {
                    throw new DecodeException("Found unknown tag while reading a list!");
                }
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(decode(ctx, dis, listType));
                }
                yield ctx.toList(list);
            }
            case COMPOUND -> {

                Map<String, T> map = new HashMap<>();
                TagType nextTag;
                while((nextTag = readTagType(dis)) != TagType.END) {
                    map.put(dis.readUTF(), decode(ctx, dis, nextTag));
                }

                yield ctx.toMap(map);
            }
            case INT_ARRAY -> {
                List<T> list = new ArrayList<>();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(ctx.toNumber(dis.readInt()));
                }
                listType = TagType.INT;
                yield ctx.toList(list);
            }
            case LONG_ARRAY -> {

                List<T> list = new ArrayList<>();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(ctx.toNumber(dis.readLong()));
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
