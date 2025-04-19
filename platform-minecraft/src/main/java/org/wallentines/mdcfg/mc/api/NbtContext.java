package org.wallentines.mdcfg.mc.api;

import net.minecraft.nbt.*;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NbtContext implements SerializeContext<Tag> {

    public static final NbtContext INSTANCE = new NbtContext();

    @Override
    public SerializeResult<String> asString(Tag object) {
        return object.asString().map(SerializeResult::success).orElseGet(() -> SerializeResult.failure("Not a string!"));
    }

    @Override
    public SerializeResult<Number> asNumber(Tag object) {
        return object.asNumber().map(SerializeResult::success).orElseGet(() -> SerializeResult.failure("Not a number!"));
    }

    @Override
    public SerializeResult<Boolean> asBoolean(Tag object) {
        return object.asBoolean().map(SerializeResult::success).orElseGet(() -> SerializeResult.failure("Not a boolean!"));
    }

    @Override
    public SerializeResult<ByteBuffer> asBlob(Tag object) {
        return object.asByteArray().map(ByteBuffer::wrap).map(SerializeResult::success).orElseGet(() -> SerializeResult.failure("Not a blob!"));
    }

    @Override
    public SerializeResult<Collection<Tag>> asList(Tag object) {
        return object.asList()
                .map(lt -> (Collection<Tag>) lt.stream().toList())
                .map(SerializeResult::success)
                .orElseGet(() -> SerializeResult.failure("Not a list!"));
    }

    @Override
    public SerializeResult<Map<String, Tag>> asMap(Tag object) {

        return object.asCompound()
                .map(cmp -> cmp.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .map(SerializeResult::success)
                .orElseGet(() -> SerializeResult.failure("Not a compound!"));
    }

    @Override
    public SerializeResult<Map<String, Tag>> asOrderedMap(Tag object) {
        return asMap(object);
    }

    @Override
    public Type getType(Tag object) {

        if(object.asNumber().isPresent()) return Type.NUMBER;
        if(object.asString().isPresent()) return Type.STRING;
        if(object.asBoolean().isPresent()) return Type.BOOLEAN;
        if(object.getType() == ByteArrayTag.TYPE) return Type.BLOB;
        if(object.asList().isPresent()) return Type.LIST;
        if(object.asCompound().isPresent()) return Type.MAP;

        return Type.UNKNOWN;

    }

    @Override
    public Collection<String> getOrderedKeys(Tag object) {
        if(object.asCompound().isPresent()) return object.asCompound().get().keySet();
        return List.of();
    }

    @Override
    public Tag get(String key, Tag object) {
        if(object.asCompound().isPresent()) return object.asCompound().get().get(key);
        return null;
    }

    @Override
    public Tag toString(String object) {
        return StringTag.valueOf(object);
    }

    @Override
    public Tag toNumber(Number object) {

        if(object instanceof Byte) return ByteTag.valueOf(object.byteValue());
        if(object instanceof Short) return ShortTag.valueOf(object.shortValue());
        if(object instanceof Integer) return IntTag.valueOf(object.intValue());
        if(object instanceof Long) return LongTag.valueOf(object.longValue());
        if(object instanceof Float) return FloatTag.valueOf(object.floatValue());
        if(object instanceof Double) return DoubleTag.valueOf(object.doubleValue());
        if(object instanceof BigInteger) return LongTag.valueOf(object.longValue());

        return DoubleTag.valueOf(object.doubleValue());
    }

    @Override
    public Tag toBoolean(Boolean object) {
        return ByteTag.valueOf(object);
    }

    @Override
    public Tag toBlob(ByteBuffer object) {
        return new ByteArrayTag(object.array());
    }

    @Override
    public Tag toList(Collection<Tag> list) {

        ListTag out = new ListTag();
        out.addAll(list);
        return out;
    }

    @Override
    public Tag toMap(Map<String, Tag> map) {

        CompoundTag out = new CompoundTag();
        for (Map.Entry<String, Tag> entry : map.entrySet()) {
            out.put(entry.getKey(), entry.getValue());
        }

        return out;
    }

    @Override
    public Tag nullValue() {
        return null;
    }

    @Override
    public Tag set(String key, Tag value, Tag object) {

        if(object == null || object.asCompound().isEmpty()) return nullValue();
        if(value == null) {
            object.asCompound().orElseThrow().remove(key);
        } else {
            object.asCompound().orElseThrow().put(key, value);
        }
        return object;
    }

    @Override
    public boolean supportsMeta(Tag object) {
        return false;
    }

    @Override
    public String getMetaProperty(Tag object, String key) {
        return null;
    }

    @Override
    public void setMetaProperty(Tag object, String key, String value) {

    }
}
