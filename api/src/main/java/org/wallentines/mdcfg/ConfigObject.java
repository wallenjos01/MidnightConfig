package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigObject {

    protected Map<String, String> meta = new HashMap<>();

    /**
     * Whether the value is a primitive value. (String, Number, or Boolean)
     * @return Whether the value is a primitive.
     */
    public abstract boolean isPrimitive();

    /**
     * Whether the value is a ConfigList.
     * @return Whether the value is a ConfigList.
     */
    public abstract boolean isList();

    /**
     * Whether the value is a ConfigSection.
     * @return Whether the value is a ConfigSection.
     */
    public abstract boolean isSection();

    /**
     * Whether the value is a ConfigBlob
     * @return Whether the value is a ConfigBlob
     */
    public abstract boolean isBlob();

    /**
     * Whether the value is a String value. This also means it is a primitive
     * @return Whether the value is a String
     */
    public boolean isString() {
        return isPrimitive() && asPrimitive().isString();
    }

    /**
     * Whether the value is a Number value. This also means it is a primitive
     * @return Whether the value is a Number
     */
    public boolean isNumber() {
        return isPrimitive() && asPrimitive().isNumber();
    }

    /**
     * Whether the value is a Boolean value. This also means it is a primitive
     * @return Whether the value is a Boolean
     */
    public boolean isBoolean() {
        return isPrimitive() && asPrimitive().isBoolean();
    }

    /**
     * Casts this object to a ConfigPrimitive and returns it
     * @return This object as a ConfigPrimitive
     * @throws IllegalStateException If the value is not a primitive
     */
    public abstract ConfigPrimitive asPrimitive();

    /**
     * Casts this object to a ConfigList and returns it
     * @return This object as a ConfigList
     * @throws IllegalStateException If the value is not a list
     */
    public abstract ConfigList asList();

    /**
     * Casts this object to a ConfigSection and returns it
     * @return This object as a ConfigSection
     * @throws IllegalStateException If the value is not a section
     */
    public abstract ConfigSection asSection();

    /**
     * Casts this object to a ConfigBlob and returns it
     * @return This object as a ConfigBlob
     * @throws IllegalStateException If the value is not a blob
     */
    public abstract ConfigBlob asBlob();

    /**
     * Copies this object
     * @return A copy of this object
     */
    public abstract ConfigObject copy();

    /**
     * Casts this object to a String and returns it
     * @return This object as a String
     * @throws IllegalStateException If the value is not a String
     */
    public String asString() {
        return asPrimitive().asString();
    }

    /**
     * Casts this object to a Number and returns it
     * @return This object as a Number
     * @throws IllegalStateException If the value is not a Number
     */
    public Number asNumber() {
        return asPrimitive().asNumber();
    }

    /**
     * Casts this object to a Boolean and returns it
     * @return This object as a Boolean
     * @throws IllegalStateException If the value is not a Boolean
     */
    public Boolean asBoolean() {
        return asPrimitive().asBoolean();
    }

    /**
     * Adds a meta property to this config object, which cna be queried layer, and may or may not be serialized
     * @param key The key of the property to set
     * @param value The property value
     */
    public void setMetaProperty(String key, String value) {
        meta.put(key, value);
    }

    /**
     * Queries a meta property for this config object
     * @param key The key of the property to get
     * @return The value of the property
     */
    public String getMetaProperty(String key) {
        return meta.get(key);
    }

    /**
     * Attempts to convert the given object to a ConfigObject
     * @param obj The object to attempt to convert
     * @return The object converted to a ConfigObject
     * @throws IllegalArgumentException If the object cannot be converted
     */
    public static ConfigObject toConfigObject(Object obj) {

        // Config Objects
        if(obj instanceof ConfigObject) return (ConfigObject) obj;

        // Primitives
        if(obj instanceof Number) {
            return new ConfigPrimitive((Number) obj);
        }

        if(obj instanceof String) {
            return new ConfigPrimitive((String) obj);
        }

        if(obj instanceof Boolean) {
            return new ConfigPrimitive((Boolean) obj);
        }

        // Lists
        if(obj instanceof Collection<?>) {
            return ConfigList.of((Collection<?>) obj);
        }

        // Maps
        if(obj instanceof Map<?, ?>) {
            return ConfigSection.of((Map<?, ?>) obj);
        }

        // Blobs
        if(obj instanceof ByteBuffer) {
            return new ConfigBlob((ByteBuffer) obj);
        } else if(obj instanceof byte[]) {
            return new ConfigBlob((byte[]) obj);
        }

        throw new IllegalArgumentException("Unable to convert " + obj + " to a config object!");
    }

    public static final Serializer<ConfigObject> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, ConfigObject value) {
            return SerializeResult.ofNullable(ConfigContext.INSTANCE.convert(context, value), "Could not convert " + value + " to ConfigObject!");
        }
        @Override
        public <O> SerializeResult<ConfigObject> deserialize(SerializeContext<O> context, O value) {
            return SerializeResult.ofNullable(context.convert(ConfigContext.INSTANCE, value), "Could not read " + value + " as ConfigObject!");
        }
    };

}
