package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.SerializeContext;

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
            return TagType.parse(ctx.getMetaProperty(obj, "nbt.tag_type"));
        }
        return null;
    }
    
}
