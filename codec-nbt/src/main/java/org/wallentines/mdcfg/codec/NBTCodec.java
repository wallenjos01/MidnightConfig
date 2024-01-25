package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class NBTCodec implements Codec {

    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_FLOAT = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private static final int TAG_LONG_ARRAY = 12;


    private final boolean expectRootName;

    public NBTCodec(boolean expectRootName) {
        this.expectRootName = expectRootName;
    }

    @Override
    public <T> void encode(@NotNull SerializeContext<T> ctx, T t, @NotNull OutputStream os, Charset charset) throws EncodeException, IOException {

        DataOutput dos = new DataOutputStream(os);

        int type = getTagType(ctx, t);
        dos.writeByte(type);

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

        int type = getTagType(ctx, t);
        if (type == -1) {
            throw new EncodeException("Unable to determine NBT type of " + t + "!");
        }

        switch (type) {
            case TAG_BYTE -> dos.writeByte(ctx.asNumber(t).byteValue());
            case TAG_SHORT -> dos.writeShort(ctx.asNumber(t).shortValue());
            case TAG_INT -> dos.writeInt(ctx.asNumber(t).intValue());
            case TAG_LONG -> dos.writeLong(ctx.asNumber(t).longValue());
            case TAG_FLOAT -> dos.writeFloat(ctx.asNumber(t).floatValue());
            case TAG_DOUBLE -> dos.writeDouble(ctx.asNumber(t).doubleValue());
            case TAG_BYTE_ARRAY, TAG_INT_ARRAY, TAG_LONG_ARRAY -> {
                Collection<T> list = ctx.asList(t);
                dos.writeInt(list.size());
                for(T t1 : list) {
                    encode(ctx, t1, dos);
                }
            }
            case TAG_STRING -> dos.writeUTF(ctx.asString(t));
            case TAG_LIST -> {
                Collection<T> list = ctx.asList(t);
                dos.writeByte(getListType(ctx, list));
                dos.writeInt(list.size());
                for(T t1 : list) {
                    encode(ctx, t1, dos);
                }
            }
            case TAG_COMPOUND -> {
                Map<String, T> values = ctx.asMap(t);
                for(Map.Entry<String, T> ent : values.entrySet()) {
                    dos.writeByte(getTagType(ctx, ent.getValue()));
                    dos.writeUTF(ent.getKey());
                    encode(ctx, ent.getValue(), dos);
                }
                dos.writeByte(TAG_END);
            }
        }
    }

    private <T> int getTagType(SerializeContext<T> ctx, T value) {

        if(ctx.supportsMeta(value)) {
            String type = ctx.getMetaProperty(value, "nbt.tag_type");
            if(type != null && type.length() == 1) {
                char c = type.charAt(0);
                if(HexFormat.isHexDigit(c)) {
                    return HexFormat.fromHexDigit(c);
                }
            }
        }

        if(ctx.isNumber(value)) {
            Number num = ctx.asNumber(value);
            if(num instanceof Byte) {
                return TAG_BYTE;
            }
            else if(num instanceof Short) {
                return TAG_SHORT;
            }
            else if(num instanceof Integer) {
                return TAG_INT;
            }
            else if(num instanceof Long) {
                return TAG_LONG;
            }
            else if(num instanceof Float) {
                return TAG_FLOAT;
            }
            else if(num instanceof Double) {
                return TAG_DOUBLE;
            }
        }
        else if(ctx.isString(value)) {
            return TAG_STRING;
        }
        else if(ctx.isList(value)) {

            int tag = -1;
            if (ctx.supportsMeta(value)) {
                String tagStr = ctx.getMetaProperty(value, "nbt.list_type");
                switch (tagStr) {
                    case "byte" -> tag = TAG_BYTE_ARRAY;
                    case "int" -> tag = TAG_INT_ARRAY;
                    case "long" -> tag = TAG_LONG_ARRAY;
                    case "tag" -> tag = TAG_LIST;
                }
            }

            Collection<T> values = ctx.asList(value);
            if(tag == -1) {
                tag = getListType(ctx, values);
            }

            if(tag == -1) return -1;

            return switch (tag) {
                case TAG_BYTE -> TAG_BYTE_ARRAY;
                case TAG_INT -> TAG_INT_ARRAY;
                case TAG_LONG -> TAG_LONG_ARRAY;
                default -> TAG_LIST;
            };
        } else if(ctx.isMap(value)) {
            return TAG_COMPOUND;
        }

        return -1;
    }

    private <T> int getListType(SerializeContext<T> ctx, Collection<T> values) {

        if(values.isEmpty()) {
            return TAG_END;
        }

        int tag = -1;
        for(T val : values) {
            if(tag == -1) {
                tag = getTagType(ctx, val);
            } else {
                if(tag != getTagType(ctx, val)) {
                    return -1;
                }
            }
        }

        return tag;
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> ctx, @NotNull InputStream is, Charset charset) throws DecodeException, IOException {

        DataInput dis = new DataInputStream(is);

        int tag = dis.readByte();
        String rootName = null;
        if(expectRootName) {
            if(tag != TAG_COMPOUND) {
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

    private <T> T decode(@NotNull SerializeContext<T> ctx, @NotNull DataInput dis, int tag) throws IOException {

        int listType = -1;

        T out = switch(tag) {
            default -> throw new DecodeException("Found unknown tag " + tag + "!");
            case TAG_END -> throw new DecodeException("Found unexpected end tag!");
            case TAG_BYTE -> ctx.toNumber(dis.readByte());
            case TAG_SHORT -> ctx.toNumber(dis.readShort());
            case TAG_INT -> ctx.toNumber(dis.readInt());
            case TAG_LONG -> ctx.toNumber(dis.readLong());
            case TAG_FLOAT -> ctx.toNumber(dis.readFloat());
            case TAG_DOUBLE -> ctx.toNumber(dis.readDouble());
            case TAG_BYTE_ARRAY -> {
                List<T> list = new ArrayList<>();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(ctx.toNumber(dis.readByte()));
                }
                listType = TAG_BYTE;
                yield ctx.toList(list);
            }
            case TAG_STRING -> ctx.toString(dis.readUTF());
            case TAG_LIST -> {
                List<T> list = new ArrayList<>();
                listType = dis.readByte();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(decode(ctx, dis, listType));
                }
                yield ctx.toList(list);
            }
            case TAG_COMPOUND -> {

                Map<String, T> map = new HashMap<>();
                int nextTag;
                while((nextTag = dis.readByte()) != TAG_END) {
                    map.put(dis.readUTF(), decode(ctx, dis, nextTag));
                }

                yield  ctx.toMap(map);
            }
            case TAG_INT_ARRAY -> {
                List<T> list = new ArrayList<>();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(ctx.toNumber(dis.readInt()));
                }
                listType = TAG_INT;
                yield ctx.toList(list);
            }
            case TAG_LONG_ARRAY -> {

                List<T> list = new ArrayList<>();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(ctx.toNumber(dis.readLong()));
                }
                listType = TAG_LONG;
                yield ctx.toList(list);
            }
        };

        if(ctx.supportsMeta(out)) {
            if(listType != -1) {
                ctx.setMetaProperty(out, "nbt.list_type", Integer.toHexString(listType));
            }
            ctx.setMetaProperty(out, "nbt.tag_type", Integer.toHexString(tag));
        }

        return out;
    }
}
