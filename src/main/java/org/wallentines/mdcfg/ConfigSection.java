package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ConfigSection implements ConfigObject {

    private final List<ConfigObject> values = new ArrayList<>();
    private final List<String> orderedKeys = new ArrayList<>();
    private final HashMap<String, Integer> indicesByKey = new HashMap<>();

    public ConfigSection() { }

    public ConfigObject set(String key, ConfigObject value) {

        if(value == null) {

            // Return early if removing
            return remove(key);
        }

        Integer index = indicesByKey.get(key);
        if(index == null) {

            // Add new item
            index = values.size();
            indicesByKey.put(key, index);

            orderedKeys.add(key);
            values.add(value);

            return null;

        } else {

            // Update existing item
            return values.set(index, value);
        }
    }


    public <T> ConfigObject set(String key, T value, Serializer<T> serializer) {
        return set(key, serializer.serialize(ConfigContext.INSTANCE, value).getOrThrow());
    }

    public ConfigObject set(String key, String value) {
        return set(key, new ConfigPrimitive(value));
    }

    public ConfigObject set(String key, Number value) {
        return set(key, new ConfigPrimitive(value));
    }

    public ConfigObject set(String key, Boolean value) {
        return set(key, new ConfigPrimitive(value));
    }

    public ConfigObject get(String key) {

        Integer index = indicesByKey.get(key);
        if(index == null) return null;

        return values.get(index);
    }

    public <T> T get(String key, Serializer<T> serializer) {

        ConfigObject out = get(key);
        return serializer.deserialize(ConfigContext.INSTANCE, out).getOrThrow();
    }

    public <T> Optional<T> getOptional(String key, Serializer<T> serializer) {

        ConfigObject out = get(key);
        return serializer.deserialize(ConfigContext.INSTANCE, out).get();
    }

    public Optional<ConfigObject> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    public String getOrDefault(String key, String defaultValue) {
        return getOptional(key).filter(ConfigObject::isString).map(ConfigObject::asString).orElse(defaultValue);
    }

    public Number getOrDefault(String key, Number defaultValue) {
        return getOptional(key).filter(ConfigObject::isNumber).map(ConfigObject::asNumber).orElse(defaultValue);
    }

    public Boolean getOrDefault(String key, Boolean defaultValue) {
        return getOptional(key).filter(ConfigObject::isBoolean).map(ConfigObject::asBoolean).orElse(defaultValue);
    }


    public String getString(String key) {
        return get(key).asPrimitive().asString();
    }

    public Number getNumber(String key) {
        return get(key).asPrimitive().asNumber();
    }

    public byte getByte(String key) {
        return getNumber(key).byteValue();
    }

    public short getShort(String key) {
        return getNumber(key).shortValue();
    }

    public int getInt(String key) {
        return getNumber(key).intValue();
    }

    public long getLong(String key) {
        return getNumber(key).longValue();
    }

    public float getFloat(String key) {
        return getNumber(key).floatValue();
    }

    public double getDouble(String key) {
        return getNumber(key).doubleValue();
    }

    public boolean getBoolean(String key) {
        return get(key).asPrimitive().asBoolean();
    }

    public ConfigList getList(String key) {
        return get(key).asList();
    }

    public <T> List<T> getListFiltered(String key, Serializer<T> serializer) {
        ConfigList list = get(key).asList();
        List<T> out = new ArrayList<>();
        for(ConfigObject obj : list.values()) {
            SerializeResult<T> res = serializer.deserialize(ConfigContext.INSTANCE, obj);
            if(res.isComplete()) out.add(res.getOrThrow());
        }
        return out;
    }

    public ConfigSection getSection(String key) {
        return get(key).asSection();
    }

    public ConfigSection getOrCreateSection(String key) {
        ConfigObject obj = get(key);
        if(obj == null) {
            ConfigSection out = new ConfigSection();
            set(key, out);
            return out;
        }
        if(obj.isSection()) {
            return obj.asSection();
        }
        throw new IllegalStateException("There is already a value with key " + key + ", and it is not a ConfigSection!");
    }

    public boolean has(String key) {
        return indicesByKey.containsKey(key);
    }

    public boolean hasList(String key) {
        return has(key) && values.get(indicesByKey.get(key)).isList();
    }

    public boolean hasSection(String key) {
        return has(key) && values.get(indicesByKey.get(key)).isSection();
    }

    public ConfigObject remove(String key) {

        Integer index = indicesByKey.get(key);
        if(index == null) return null;

        // Remove object
        ConfigObject out = values.remove(index.intValue()); // Explicitly cast to a primitive int so remove-by-index is used
        orderedKeys.remove(index.intValue());

        indicesByKey.remove(key);

        // Fix other keys
        for(String otherKey : indicesByKey.keySet()) {
            int otherIndex = indicesByKey.get(otherKey);
            if(otherIndex > index) {
                indicesByKey.put(otherKey, otherIndex - 1);
            }
        }

        return out;
    }

    public void fill(ConfigSection other) {
        for(String key : other.orderedKeys) {
            if(!has(key)) set(key, other.get(key));
        }
    }

    public void fillOverwrite(ConfigSection other) {
        for(String key : other.orderedKeys) {
            set(key, other.get(key));
        }
    }

    public ConfigSection copy() {

        ConfigSection out = new ConfigSection();
        for(String key : orderedKeys) {
            out.set(key, get(key).copy());
        }
        return out;
    }

    public int size() {

        return values.size();
    }

    public Collection<String> getKeys() {
        return List.copyOf(orderedKeys);
    }

    public ConfigSection with(String key, ConfigObject value) {
        set(key, value);
        return this;
    }

    public <T> ConfigSection with(String key, T value, Serializer<T> serializer) {
        set(key, value, serializer);
        return this;
    }

    public ConfigSection with(String key, String value) {
        set(key, value);
        return this;
    }

    public ConfigSection with(String key, Number value) {
        set(key, value);
        return this;
    }

    public ConfigSection with(String key, Boolean value) {
        set(key, value);
        return this;
    }

    public Stream<Tuples.T2<String, ConfigObject>> stream() {

        return orderedKeys.stream().map(key -> new Tuples.T2<>(key, get(key)));
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isSection() {
        return true;
    }

    @Override
    public ConfigPrimitive asPrimitive() {
        throw new IllegalStateException("Unable to convert a section to a primitive!");
    }

    @Override
    public ConfigList asList() {
        throw new IllegalStateException("Unable to convert a section to a list!");
    }

    @Override
    public ConfigSection asSection() {
        return this;
    }

    @Override
    public String toString() {
        return "ConfigSection{" +
                "size=" + size() +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ConfigSection otherSection = (ConfigSection) other;

        if(values.size() != otherSection.values.size()) return false;
        if(orderedKeys.size() != otherSection.orderedKeys.size()) return false;

        for(String key : orderedKeys) {
            ConfigObject obj = get(key);
            ConfigObject otherObj = otherSection.get(key);

            if(!obj.equals(otherObj)) return false;
        }

        return true;
    }

    public static ConfigSection of(Map<?, ?> map) {

        ConfigSection sec = new ConfigSection();
        map.forEach((key, value) -> {

            if(!(key instanceof String)) throw new IllegalArgumentException("Unable to convert map with non-string keys to config section!");
            sec.set((String) key, ConfigObject.toConfigObject(value));
        });

        return sec;
    }

    public static final Serializer<ConfigSection> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, ConfigSection value) {
            return SerializeResult.ofNullable(ConfigContext.INSTANCE.convert(context, value), "Could not convert " + value + " to ConfigObject!");
        }
        @Override
        public <O> SerializeResult<ConfigSection> deserialize(SerializeContext<O> context, O value) {
            return SerializeResult.ofNullable(context.convert(ConfigContext.INSTANCE, value), "Could not read " + value + " as ConfigObject!").map(obj -> {
                if(!obj.isSection()) return SerializeResult.failure(value + " is not a ConfigSection!");
                return SerializeResult.success(obj.asSection());
            });
        }
    };
}
