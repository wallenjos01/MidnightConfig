package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link SerializeContext SerializeContext} for {@link ConfigObject ConfigObjects}
 */
public class ConfigContext implements SerializeContext<ConfigObject> {

    public static final ConfigContext INSTANCE = new ConfigContext();

    @Override
    public String asString(ConfigObject object) {
        if(!isString(object)) return null;
        return object.asPrimitive().asString();
    }

    @Override
    public Number asNumber(ConfigObject object) {
        if(!isNumber(object)) return null;
        return object.asPrimitive().asNumber();
    }

    @Override
    public Boolean asBoolean(ConfigObject object) {
        if(!isBoolean(object)) return null;
        return object.asPrimitive().asBoolean();
    }

    @Override
    public Collection<ConfigObject> asList(ConfigObject object) {
        if(!isList(object)) return null;
        return object.asList().values();
    }

    @Override
    public Map<String, ConfigObject> asMap(ConfigObject object) {
        if(!isMap(object)) return null;
        return object.asSection().stream().collect(Collectors.toMap(t2 -> t2.p1, t2 -> t2.p2));
    }

    @Override
    public Map<String, ConfigObject> asOrderedMap(ConfigObject object) {
        if(!isMap(object)) return null;

        LinkedHashMap<String, ConfigObject> out = new LinkedHashMap<>();
        for(String key : object.asSection().getKeys()) {
            out.put(key, object.asSection().get(key));
        }
        return out;
    }

    @Override
    public boolean isString(ConfigObject object) {
        return object != null && object.isPrimitive() && object.asPrimitive().isString();
    }

    @Override
    public boolean isNumber(ConfigObject object) {
        return object != null && object.isPrimitive() && object.asPrimitive().isNumber();
    }

    @Override
    public boolean isBoolean(ConfigObject object) {
        return object != null && object.isPrimitive() && object.asPrimitive().isBoolean();
    }

    @Override
    public boolean isList(ConfigObject object) {
        return object != null && object.isList();
    }

    @Override
    public boolean isMap(ConfigObject object) {
        return object != null && object.isSection();
    }

    @Override
    public Collection<String> getOrderedKeys(ConfigObject object) {
        if(object == null || !object.isSection()) return null;
        return object.asSection().getKeys();
    }

    @Override
    public ConfigObject get(String key, ConfigObject object) {
        if(object == null || !object.isSection()) return null;
        return object.asSection().get(key);
    }

    @Override
    public ConfigObject toString(String object) {
        if(object == null) return null;
        return new ConfigPrimitive(object);
    }

    @Override
    public ConfigObject toNumber(Number object) {
        if(object == null) return null;
        return new ConfigPrimitive(object);
    }

    @Override
    public ConfigObject toBoolean(Boolean object) {
        if(object == null) return null;
        return new ConfigPrimitive(object);
    }

    @Override
    public ConfigObject toList(Collection<ConfigObject> object) {
        if(object == null) return null;
        return ConfigList.of(object);
    }

    @Override
    public ConfigObject toMap(Map<String, ConfigObject> object) {
        if(object == null) return null;
        ConfigSection out = new ConfigSection();
        object.forEach(out::set);
        return out;
    }

    @Override
    public ConfigObject mergeList(Collection<ConfigObject> list, ConfigObject base) {
        if(!base.isList()) return null;
        if(list == null) return base;

        ConfigList out = base.asList();
        out.addAll(list);

        return out;
    }

    @Override
    public ConfigObject mergeMap(ConfigObject object, ConfigObject other) {
        if(object == null || !object.isSection() || other == null || !other.isSection()) return null;
        object.asSection().fill(other.asSection());
        return object;
    }

    @Override
    public ConfigObject mergeMapOverwrite(ConfigObject object, ConfigObject other) {
        if(object == null || !object.isSection() || other == null || !other.isSection()) return null;
        object.asSection().fillOverwrite(other.asSection());
        return object;
    }

    @Override
    public ConfigObject set(String key, ConfigObject value, ConfigObject object) {
        if(object == null || !object.isSection()) return null;
        object.asSection().set(key, value);
        return object;
    }

    @Override
    public ConfigObject copy(ConfigObject object) {
        return object.copy();
    }
}
