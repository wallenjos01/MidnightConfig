package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.util.Collection;
import java.util.UUID;

public class NBTUtil {

    public static ConfigObject makeUUID(UUID uuid, boolean array) {
        if(array) {
            long u1 = uuid.getMostSignificantBits();
            long u2 = uuid.getLeastSignificantBits();
            int[] values =  new int[] { (int) (u1 >> 32), (int) u1, (int) (u2 >> 32), (int) u2 };

            return makeIntArray(values);

        } else {
            return new ConfigPrimitive(uuid.toString());
        }
    }

    public static ConfigList makeByteArray(byte... array) {
        ConfigList out = new ConfigList();
        for(byte b : array) out.add(b);
        setTagType(out, TagType.BYTE_ARRAY);
        return out;
    }

    public static ConfigList makeIntArray(int... array) {
        ConfigList out = new ConfigList();
        for(int b : array) out.add(b);
        setTagType(out, TagType.INT_ARRAY);
        return out;
    }

    public static ConfigList makeLongArray(long... array) {
        ConfigList out = new ConfigList();
        for(long b : array) out.add(b);
        setTagType(out, TagType.LONG_ARRAY);
        return out;
    }
    
    public static void setTagType(ConfigObject obj, TagType tagType) {
        obj.setMetaProperty("nbt.tag_type", tagType.encode());
    }
    
    public static TagType getTagType(ConfigObject obj) {
        return TagType.parse(obj.getMetaProperty("nbt.tag_type"));
    }


    public static <T> void setTagType(SerializeContext<T> ctx, T obj, TagType tagType) {
        if(ctx.supportsMeta(obj)) {
            ctx.setMetaProperty(obj, "nbt.tag_type", tagType.encode());
        }
    }

    public static <T> TagType getTagType(SerializeContext<T> ctx, T obj) {

        if(ctx.supportsMeta(obj)) {
            TagType type = TagType.parse(ctx.getMetaProperty(obj, "nbt.tag_type"));
            if(type != null) return type;
        }

        switch (ctx.getType(obj)) {
            case STRING: return TagType.STRING;
            case NUMBER: {
                Number num = ctx.asNumber(obj).getOrThrow();
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
            case BOOLEAN: return TagType.BYTE;
            case BLOB: return TagType.BYTE_ARRAY;
            case LIST: {
                TagType type;
                if (ctx.supportsMeta(obj)) {
                    String tagStr = ctx.getMetaProperty(obj, "nbt.list_type");
                    type = TagType.parse(tagStr);
                    if(type != null) return type;
                }

                Collection<T> values = ctx.asList(obj).getOrThrow();
                type = getListType(ctx, values);

                switch (type) {
                    case BYTE:
                        return TagType.BYTE_ARRAY;
                    case INT:
                        return TagType.INT_ARRAY;
                    case LONG:
                        return TagType.LONG_ARRAY;
                    default:
                        return TagType.LIST;
                }
            }
            case MAP: return TagType.COMPOUND;
            default:
                return null;
        }
    }

    public static <T> TagType getListType(SerializeContext<T> ctx, Collection<T> values) {

        if(values.isEmpty()) {
            return TagType.END;
        }

        TagType tag = null;
        for(T val : values) {
            if(tag == null) {
                tag = getTagType(ctx, val);
            } else {
                if(tag != getTagType(ctx, val)) {
                    return null;
                }
            }
        }

        return tag;
    }
    
}
