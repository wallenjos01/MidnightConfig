package org.wallentines.mdcfg.registry;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.InlineSerializer;

import java.util.List;
import java.util.Map;

/**
 * A Registry which contains a default key and value
 * @param <I> The key type for this registry
 * @param <T> The value type for this registry
 */
public class DefaultedRegistry<I, T> extends Registry<I, T> {

    private final I defaultKey;
    private final T defaultValue;

    public DefaultedRegistry(I defaultKey, T defaultValue, InlineSerializer<I> idSerializer) {
        super(idSerializer);
        this.defaultKey = defaultKey;
        this.defaultValue = defaultValue;
    }

    public DefaultedRegistry(I defaultKey, T defaultValue, InlineSerializer<I> idSerializer, boolean allowDuplicateValues, boolean allowNullValues, boolean allowEqualValues) {
        super(idSerializer, allowDuplicateValues, allowNullValues, allowEqualValues);
        this.defaultKey = defaultKey;
        this.defaultValue = defaultValue;
    }

    protected DefaultedRegistry(I defaultKey, T defaultValue, InlineSerializer<I> idSerializer, boolean allowDuplicateValues, boolean allowNullValues, boolean allowEqualValues, List<I> ids, List<T> values, Map<I, Integer> indexById, Map<T, Integer> indexByValue) {
        super(idSerializer, allowDuplicateValues, allowNullValues, allowEqualValues, ids, values, indexById, indexByValue);
        this.defaultKey = defaultKey;
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the default key for this registry
     * @return The default key
     */
    public I getDefaultKey() {
        return defaultKey;
    }

    /**
     * Gets the default value for this registry
     * @return The default value
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T get(@NotNull I id) {
        T out = super.get(id);
        if(out == null) {
            return defaultValue;
        }
        return out;
    }

    @Override
    public I getId(@NotNull T value) {
        I out = super.getId(value);
        if(out == null) {
            return defaultKey;
        }
        return out;
    }


    /**
     * Creates an immutable registry which contains all keys and values of this registry, but cannot be modified
     * @return A frozen registry
     */
    public Registry.Frozen<I, T> freeze() {
        return new DefaultedRegistry.Frozen<>(this, idSerializer);
    }

    /**
     * An immutable defaulted registry
     * @param <I> The type of IDs in this registry
     * @param <T> The type of values in this registry
     */
    protected static class Frozen<I, T> extends Registry.Frozen<I, T> {

        private final I defaultKey;
        private final T defaultValue;

        public Frozen(DefaultedRegistry<I, T> registry, InlineSerializer<I> idSerializer) {
            super(registry, idSerializer);
            this.defaultKey = registry.defaultKey;
            this.defaultValue = registry.defaultValue;
        }

        @Override
        public T get(@NotNull I id) {
            T out = super.get(id);
            if(out == null) {
                return defaultValue;
            }
            return out;
        }

        @Override
        public I getId(@NotNull T value) {
            I out = super.getId(value);
            if(out == null) {
                return defaultKey;
            }
            return out;
        }
    }
}
