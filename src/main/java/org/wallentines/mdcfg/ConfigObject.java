package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.Collection;
import java.util.Map;

public interface ConfigObject {

    boolean isPrimitive();

    boolean isList();

    boolean isSection();

    default boolean isString() {
        return isPrimitive() && asPrimitive().isString();
    }

    default boolean isNumber() {
        return isPrimitive() && asPrimitive().isNumber();
    }

    default boolean isBoolean() {
        return isPrimitive() && asPrimitive().isBoolean();
    }

    ConfigPrimitive asPrimitive();

    ConfigList asList();

    ConfigSection asSection();

    ConfigObject copy();

    default String asString() {
        return asPrimitive().asString();
    }

    default Number asNumber() {
        return asPrimitive().asNumber();
    }

    default Boolean asBoolean() {
        return asPrimitive().asBoolean();
    }


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
