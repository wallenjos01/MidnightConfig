package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.*;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link SerializeContext SerializeContext} for {@link ConfigObject ConfigObjects}
 */
public class ConfigContext implements SerializeContext<ConfigObject> {

    public static final ConfigContext INSTANCE = new ConfigContext();

    @Override
    public SerializeResult<String> asString(ConfigObject object) {
        if(!isString(object)) return SerializeResult.failure("Not a string");
        return SerializeResult.success(object.asPrimitive().asString());
    }

    @Override
    public SerializeResult<Number> asNumber(ConfigObject object) {
        if(!isNumber(object)) return SerializeResult.failure("Not a number");
        return SerializeResult.success(object.asPrimitive().asNumber());
    }

    @Override
    public SerializeResult<Boolean> asBoolean(ConfigObject object) {
        if(!isBoolean(object)) return SerializeResult.failure("Not a boolean");
        return SerializeResult.success(object.asPrimitive().asBoolean());
    }

    @Override
    public SerializeResult<ByteBuffer> asBlob(ConfigObject object) {
        if(!isBlob(object)) return SerializeResult.failure("Not a blob");
        return SerializeResult.success(object.asBlob().getData());
    }

    @Override
    public SerializeResult<Collection<ConfigObject>> asList(ConfigObject object) {
        if(!isList(object)) return SerializeResult.failure("Not a list");
        return SerializeResult.success(object.asList().values());
    }

    @Override
    public SerializeResult<Map<String, ConfigObject>> asMap(ConfigObject object) {
        if(!isMap(object)) return SerializeResult.failure("Not a map!");
        return SerializeResult.success(
                object.asSection()
                        .stream()
                        .collect(Collectors.toMap(t2 -> t2.p1, t2 -> t2.p2)));
    }

    @Override
    public SerializeResult<Map<String, ConfigObject>> asOrderedMap(ConfigObject object) {
        if(!isMap(object)) return SerializeResult.failure("Not a map!");

        LinkedHashMap<String, ConfigObject> out = new LinkedHashMap<>();
        for(String key : object.asSection().getKeys()) {
            out.put(key, object.asSection().get(key));
        }
        return SerializeResult.success(out);
    }

    @Override
    public Type getType(ConfigObject object) {
        if(object == null) return Type.UNKNOWN;
        return object.getSerializedType();
    }

    @Override
    public Collection<String> getOrderedKeys(ConfigObject object) {
        if(!object.isSection()) return null;
        return object.asSection().getKeys();
    }

    @Override
    public ConfigObject get(String key, ConfigObject object) {
        if(!object.isSection()) return nullValue();
        return object.asSection().get(key);
    }

    @Override
    public ConfigObject toString(String object) {
        return new ConfigPrimitive(object);
    }

    @Override
    public ConfigObject toNumber(Number object) {
        return new ConfigPrimitive(object);
    }

    @Override
    public ConfigObject toBoolean(Boolean object) {
        return new ConfigPrimitive(object);
    }

    @Override
    public ConfigObject toBlob(ByteBuffer object) {
        return new ConfigBlob(object);
    }

    @Override
    public ConfigObject toList(Collection<ConfigObject> object) {
        return ConfigList.of(object);
    }

    @Override
    public ConfigObject toMap(Map<String, ConfigObject> object) {
        ConfigSection out = new ConfigSection();
        object.forEach(out::set);
        return out;
    }

    @Override
    public ConfigObject nullValue() {
        return ConfigPrimitive.NULL;
    }

    @Override
    public ConfigObject mergeList(Collection<ConfigObject> list, ConfigObject base) {
        if(!base.isList()) return nullValue();
        if(list == null) return base;

        ConfigList out = base.asList();
        out.addAll(list);

        return out;
    }

    @Override
    public ConfigObject mergeMap(ConfigObject object, ConfigObject other) {
        if(object == null || !object.isSection() || other == null || !other.isSection()) return nullValue();
        object.asSection().fill(other.asSection());
        return object;
    }

    @Override
    public ConfigObject mergeMapOverwrite(ConfigObject object, ConfigObject other) {
        if(object == null || !object.isSection() || other == null || !other.isSection()) return nullValue();
        object.asSection().fillOverwrite(other.asSection());
        return object;
    }

    @Override
    public ConfigObject set(String key, ConfigObject value, ConfigObject object) {
        if(object == null || !object.isSection()) return nullValue();
        object.asSection().set(key, value);
        return object;
    }

    @Override
    public ConfigObject copy(ConfigObject object) {
        return object.copy();
    }

    @Override
    public boolean supportsMeta(ConfigObject object) {
        return true;
    }

    @Override
    public String getMetaProperty(ConfigObject object, String key) {
        return object.getMetaProperty(key);
    }

    @Override
    public void setMetaProperty(ConfigObject object, String key, String value) {
        object.setMetaProperty(key, value);
    }
}
