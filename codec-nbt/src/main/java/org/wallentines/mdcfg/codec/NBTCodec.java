package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
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

        TagType type = getTagType(ctx, t);
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

        TagType type = getTagType(ctx, t);

        switch (type) {
            case BYTE -> dos.writeByte(ctx.isBoolean(t) ? ctx.asBoolean(t) ? 0b1 : 0b0 : ctx.asNumber(t).byteValue());
            case SHORT -> dos.writeShort(ctx.asNumber(t).shortValue());
            case INT -> dos.writeInt(ctx.asNumber(t).intValue());
            case LONG -> dos.writeLong(ctx.asNumber(t).longValue());
            case FLOAT -> dos.writeFloat(ctx.asNumber(t).floatValue());
            case DOUBLE -> dos.writeDouble(ctx.asNumber(t).doubleValue());
            case STRING -> dos.writeUTF(ctx.asString(t));
            case BYTE_ARRAY, INT_ARRAY, LONG_ARRAY -> {
                Collection<T> list = ctx.asList(t);
                dos.writeInt(list.size());
                for(T t1 : list) {
                    encode(ctx, t1, dos);
                }
            }
            case LIST -> {
                Collection<T> list = ctx.asList(t);
                dos.writeByte(getListType(ctx, list).getValue());
                dos.writeInt(list.size());
                for(T t1 : list) {
                    encode(ctx, t1, dos);
                }
            }
            case COMPOUND -> {
                Map<String, T> values = ctx.asMap(t);
                for(Map.Entry<String, T> ent : values.entrySet()) {

                    dos.writeByte(getTagType(ctx, ent.getValue()).getValue());
                    dos.writeUTF(ent.getKey());
                    encode(ctx, ent.getValue(), dos);
                }
                dos.writeByte(TagType.END.getValue());
            }
        }
    }

    private <T> TagType getTagType(SerializeContext<T> ctx, T value) {

        if(ctx.supportsMeta(value)) {
            TagType type = TagType.parse(ctx.getMetaProperty(value, "nbt.tag_type"));
            if(type != null) return type;
        }

        if(ctx.isNumber(value)) {
            Number num = ctx.asNumber(value);
            if(num instanceof Byte) {
                return TagType.BYTE;
            }
            else if(num instanceof Short) {
                return TagType.SHORT;
            }
            else if(num instanceof Integer) {
                return TagType.INT;
            }
            else if(num instanceof Long) {
                return TagType.LONG;
            }
            else if(num instanceof Float) {
                return TagType.FLOAT;
            }
            else if(num instanceof Double) {
                return TagType.DOUBLE;
            }
        }
        else if(ctx.isBoolean(value)) {
            return TagType.BYTE;
        }
        else if(ctx.isString(value)) {
            return TagType.STRING;
        }
        else if(ctx.isList(value)) {

            TagType type;
            if (ctx.supportsMeta(value)) {
                String tagStr = ctx.getMetaProperty(value, "nbt.list_type");
                type = TagType.parse(tagStr);
                if(type != null) return type;
            }

            Collection<T> values = ctx.asList(value);
            type = getListType(ctx, values);

            return switch (type) {
                case BYTE -> TagType.BYTE_ARRAY;
                case INT -> TagType.INT_ARRAY;
                case LONG -> TagType.LONG_ARRAY;
                default -> TagType.LIST;
            };
        } else if(ctx.isMap(value)) {
            return TagType.COMPOUND;
        }

        throw new EncodeException("Unable to determine NBT type of" + value + "!");
    }

    private <T> TagType getListType(SerializeContext<T> ctx, Collection<T> values) {

        if(values.isEmpty()) {
            return TagType.END;
        }

        TagType tag = null;
        for(T val : values) {
            if(tag == null) {
                tag = getTagType(ctx, val);
            } else {
                if(tag != getTagType(ctx, val)) {
                    throw new EncodeException("Found list tag with multiple types!");
                }
            }
        }

        return tag;
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
                List<T> list = new ArrayList<>();
                int length = dis.readInt();
                for(int i = 0 ; i < length ; i++) {
                    list.add(ctx.toNumber(dis.readByte()));
                }
                listType = TagType.BYTE;
                yield ctx.toList(list);
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
