package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.Collection;
import java.util.Map;

public interface ConfigObject {

    /**
     * Whether the value is a primitive value. (String, Number, or Boolean)
     * @return Whether the value is a primitive.
     */
    boolean isPrimitive();

    /**
     * Whether the value is a ConfigList.
     * @return Whether the value is a ConfigList.
     */
    boolean isList();

    /**
     * Whether the value is a ConfigSection.
     * @return Whether the value is a ConfigSection.
     */
    boolean isSection();

    /**
     * Whether the value is a String value. This also means it is a primitive
     * @return Whether the value is a String
     */
    default boolean isString() {
        return isPrimitive() && asPrimitive().isString();
    }

    /**
     * Whether the value is a Number value. This also means it is a primitive
     * @return Whether the value is a Number
     */
    default boolean isNumber() {
        return isPrimitive() && asPrimitive().isNumber();
    }

    /**
     * Whether the value is a Boolean value. This also means it is a primitive
     * @return Whether the value is a Boolean
     */
    default boolean isBoolean() {
        return isPrimitive() && asPrimitive().isBoolean();
    }

    /**
     * Casts this object to a ConfigPrimitive and returns it
     * @return This object as a ConfigPrimitive
     * @throws IllegalStateException If the value is not a primitive
     */
    ConfigPrimitive asPrimitive();

    /**
     * Casts this object to a ConfigList and returns it
     * @return This object as a ConfigList
     * @throws IllegalStateException If the value is not a list
     */
    ConfigList asList();

    /**
     * Casts this object to a ConfigSection and returns it
     * @return This object as a ConfigSection
     * @throws IllegalStateException If the value is not a section
     */
    ConfigSection asSection();

    /**
     * Copies this object
     * @return A copy of this object
     */
    ConfigObject copy();

    /**
     * Casts this object to a String and returns it
     * @return This object as a String
     * @throws IllegalStateException If the value is not a String
     */
    default String asString() {
        return asPrimitive().asString();
    }

    /**
     * Casts this object to a Number and returns it
     * @return This object as a Number
     * @throws IllegalStateException If the value is not a Number
     */
    default Number asNumber() {
        return asPrimitive().asNumber();
    }

    /**
     * Casts this object to a Boolean and returns it
     * @return This object as a Boolean
     * @throws IllegalStateException If the value is not a Boolean
     */
    default Boolean asBoolean() {
        return asPrimitive().asBoolean();
    }

    /**
     * Attempts to convert the given object to a ConfigObject
     * @param obj The object to attempt to convert
     * @return The object converted to a ConfigObject
     * @throws IllegalArgumentException If the object cannot be converted
     */
    static ConfigObject toConfigObject(Object obj) {

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

        throw new IllegalArgumentException("Unable to convert " + obj + " to a config object!");
    }

    Serializer<ConfigObject> SERIALIZER = new Serializer<>() {
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
