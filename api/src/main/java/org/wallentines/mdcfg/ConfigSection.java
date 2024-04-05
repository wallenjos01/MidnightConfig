package org.wallentines.mdcfg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.serializer.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ConfigSection extends ConfigObject {

    private final List<ConfigObject> values = new ArrayList<>();
    private final List<String> orderedKeys = new ArrayList<>();
    private final HashMap<String, Integer> indicesByKey = new HashMap<>();

    /**
     * Creates an empty ConfigSection
     */
    public ConfigSection() { }

    /**
     * Associates the given value with the given key, overwriting any existing value if necessary
     * @param key The key to associate the given value with
     * @param value The value to put into the section. If null, the key will be removed from the map instead
     * @return A reference to the previous object associated with the given key
     */
    public ConfigObject set(String key, @Nullable ConfigObject value) {

        if(key == null) return null;

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

    /**
     * Serializes an object, then associates it with the given key, overwriting any existing value if necessary
     * @param key The key to associate the given value with
     * @param value The object to serialize
     * @param serializer The serializer to use to serialize the value
     * @return A reference to the previous object associated with the given key
     * @param <T> The type of object passed in to be serialized
     */
    public <T> ConfigObject set(String key, T value, Serializer<T> serializer) {
        if(value == null) return remove(key);
        return set(key, serializer.serialize(ConfigContext.INSTANCE, value).getOrThrow());
    }

    /**
     * Associates a given String with the given key
     * @param key The key to associate the given String with
     * @param value The String to put into the section.
     * @return A reference to the previous object associated with the given key
     */
    public ConfigObject set(String key, String value) {
        if(value == null) return remove(key);
        return set(key, new ConfigPrimitive(value));
    }

    /**
     * Associates a given Number with the given key
     * @param key The key to associate the given Number with
     * @param value The Number to put into the section.
     * @return A reference to the previous object associated with the given key
     */
    public ConfigObject set(String key, Number value) {
        if(value == null) return remove(key);
        return set(key, new ConfigPrimitive(value));
    }

    /**
     * Associates a given Boolean with the given key
     * @param key The key to associate the given Boolean with
     * @param value The Boolean to put into the section.
     * @return A reference to the previous object associated with the given key
     */
    public ConfigObject set(String key, Boolean value) {
        if(value == null) return remove(key);
        return set(key, new ConfigPrimitive(value));
    }

    /**
     * Gets a reference to the value associated with the given key, or null if not present
     * @param key The key to lookup
     * @return The value associated with the given key, or null
     */
    @Nullable
    public ConfigObject get(String key) {

        Integer index = indicesByKey.get(key);
        if(index == null) return null;

        return values.get(index);
    }
    /**
     * Gets a reference to the value associated with the given key
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the given key
     */
    public ConfigObject getOrThrow(String key) {
        return getOptional(key).orElseThrow();
    }

    /**
     * Gets a deserialized a value associated with a given key
     * @param key The key to lookup
     * @param serializer The serializer to use to deserialize the value
     * @return An instance of T deserialized from the value in the section
     * @param <T> The type of object to deserialize
     * @throws SerializeException if the value is null or cannot be converted to the requested type
     */
    public <T> T get(String key, @NotNull Serializer<T> serializer) throws SerializeException {

        ConfigObject out = get(key);
        return serializer.deserialize(ConfigContext.INSTANCE, out).getOrThrow();
    }

    /**
     * Gets a deserialized a value associated with a given key, or an empty optional if not found
     * @param key key The key to lookup
     * @param serializer The serializer to use to deserialize the value
     * @return An optional containing an instance of T deserialized from the value in the section, or empty if it could not be serialized
     * @param <T> The type of object to deserialize
     */
    public <T> Optional<T> getOptional(String key, @NotNull Serializer<T> serializer) {

        return Optional.ofNullable(get(key)).flatMap(v -> serializer.deserialize(ConfigContext.INSTANCE, v).get());
    }

    /**
     * Gets a reference to the value associated with the given key, or an empty optional if not present
     * @param key The key to lookup
     * @return An optional containing the value associated with the given key, or empty
     */
    public Optional<ConfigObject> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Gets a reference to the String associated with the given key, or the provided default value
     * @param key The key to lookup
     * @param defaultValue The value to return if the given key could not be found or is not associated with a String
     * @return The String associated with the given key, or the default value
     */
    public String getOrDefault(String key, String defaultValue) {
        return getOptional(key).filter(ConfigObject::isString).map(ConfigObject::asString).orElse(defaultValue);
    }

    /**
     * Gets a reference to the Number associated with the given key, or the provided default value
     * @param key The key to lookup
     * @param defaultValue The value to return if the given key could not be found or is not associated with a Number
     * @return The Number associated with the given key, or the default value
     */
    public Number getOrDefault(String key, Number defaultValue) {
        return getOptional(key).filter(ConfigObject::isNumber).map(ConfigObject::asNumber).orElse(defaultValue);
    }

    /**
     * Gets a reference to the Boolean associated with the given key, or the provided default value
     * @param key The key to lookup
     * @param defaultValue The value to return if the given key could not be found or is not associated with a Boolean
     * @return The Boolean associated with the given key, or the default value
     */
    public Boolean getOrDefault(String key, Boolean defaultValue) {
        return getOptional(key).filter(ConfigObject::isBoolean).map(ConfigObject::asBoolean).orElse(defaultValue);
    }

    /**
     * Gets a reference to the String associated with the given key
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a String
     */
    public String getString(String key) {
        return getOptional(key).orElseThrow().asPrimitive().asString();
    }

    /**
     * Gets a reference to the Number associated with the given key
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public Number getNumber(String key) {
        return getOptional(key).orElseThrow().asPrimitive().asNumber();
    }

    /**
     * Gets the value of the Number associated with the given key as a byte
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public byte getByte(String key) {
        return getNumber(key).byteValue();
    }

    /**
     * Gets the value of the Number associated with the given key as a short
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public short getShort(String key) {
        return getNumber(key).shortValue();
    }

    /**
     * Gets the value of the Number associated with the given key as an int
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public int getInt(String key) {
        return getNumber(key).intValue();
    }

    /**
     * Gets the value of the Number associated with the given key as a long
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public long getLong(String key) {
        return getNumber(key).longValue();
    }

    /**
     * Gets the value of the Number associated with the given key as a float
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public float getFloat(String key) {
        return getNumber(key).floatValue();
    }

    /**
     * Gets the value of the Number associated with the given key as a double
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Number
     */
    public double getDouble(String key) {
        return getNumber(key).doubleValue();
    }

    /**
     * Gets a reference to the Boolean associated with the given key
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a Boolean
     */
    public boolean getBoolean(String key) {
        return getOptional(key).orElseThrow().asPrimitive().asBoolean();
    }

    /**
     * Gets a reference to the ConfigList associated with the given key
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a ConfigList
     */
    public ConfigList getList(String key) {
        return getOptional(key).orElseThrow().asList();
    }

    /**
     * Gets a list associated with the given key, then makes a new list containing only values of type T
     * @param key The key to lookup
     * @param serializer The serializer to use to deserialize the values in the list
     * @return A new list with only elements of type T
     * @param <T> The type of objects to put in the list
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a ConfigList
     * @throws SerializeException If any object cannot be deserialized using the given serializer
     */
    public <T> List<T> getList(String key, @NotNull Serializer<T> serializer) {

        ConfigList list = getList(key);
        return new ArrayList<>(serializer.listOf().deserialize(ConfigContext.INSTANCE, list).getOrThrow());
    }

    /**
     * Gets a list associated with the given key, then makes a new list containing only values which can be serialized using the given serializer
     * @param key The key to lookup
     * @param serializer The serializer to use to deserialize the values in the list
     * @return A new list with only elements of type T
     * @param <T> The type of objects to put in the list
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a ConfigList
     */
    public <T> List<T> getListFiltered(String key, @NotNull Serializer<T> serializer) {

        ConfigList list = getList(key);
        return new ArrayList<>(serializer.filteredListOf().deserialize(ConfigContext.INSTANCE, list).getOrThrow());
    }

    /**
     * Gets a list associated with the given key, then makes a new list containing only values which can be serialized using the given serializer
     * @param key The key to lookup
     * @param serializer The serializer to use to deserialize the values in the list
     * @param onError A callback to send error text whenever an object fails to serialize
     * @return A new list with only elements of type T
     * @param <T> The type of objects to put in the list
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a ConfigList
     */
    public <T> List<T> getListFiltered(String key, @NotNull Serializer<T> serializer, Consumer<String> onError) {

        ConfigList list = getList(key);
        return new ArrayList<>(serializer.filteredListOf(onError).deserialize(ConfigContext.INSTANCE, list).getOrThrow());
    }

    /**
     * Gets a reference to the ConfigSection associated with the given key
     * @param key The key to lookup
     * @return The value associated with the given key
     * @throws NoSuchElementException If there is no value associated with the key
     * @throws IllegalStateException If the value associated with the key is not a ConfigSection
     */
    public ConfigSection getSection(String key) {
        return getOptional(key).orElseThrow().asSection();
    }

    /**
     * Gets a reference to an existing ConfigSection associated with the given key, or creates a new one, puts it in the section, and returns it
     * @param key The key to lookup
     * @return An existing or newly created ConfigSection associated with the given key
     * @throws IllegalStateException If there is already a non-ConfigSection value at the given key
     */
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

    /**
     * Determines whether there is any value associated with the given key
     * @param key The key to lookup
     * @return Whether there is any value associated with the given key
     */
    public boolean has(String key) {
        return indicesByKey.containsKey(key);
    }

    /**
     * Determines whether there is a String value associated with the given key
     * @param key The key to lookup
     * @return Whether there is a String value associated with the given key
     */
    public boolean hasString(String key) {
        return getOptional(key).filter(ConfigObject::isString).isPresent();
    }

    /**
     * Determines whether there is a Number value associated with the given key
     * @param key The key to lookup
     * @return Whether there is a Number value associated with the given key
     */
    public boolean hasNumber(String key) {
        return getOptional(key).filter(ConfigObject::isNumber).isPresent();
    }

    /**
     * Determines whether there is a Boolean value associated with the given key
     * @param key The key to lookup
     * @return Whether there is a Boolean value associated with the given key
     */
    public boolean hasBoolean(String key) {
        return getOptional(key).filter(ConfigObject::isBoolean).isPresent();
    }

    /**
     * Determines whether there is a ConfigList associated with the given key
     * @param key The key to lookup
     * @return Whether there is a ConfigList associated with the given key
     */
    public boolean hasList(String key) {
        return getOptional(key).filter(ConfigObject::isList).isPresent();
    }

    /**
     * Determines whether there is a ConfigSection associated with the given key
     * @param key The key to lookup
     * @return Whether there is a ConfigSection associated with the given key
     */
    public boolean hasSection(String key) {
        return getOptional(key).filter(ConfigObject::isSection).isPresent();
    }

    /**
     * Removes the value associated with the given key
     * @param key The key to lookup and remove
     * @return The value associated with the given key before removing, or null if there was none.
     */
    public ConfigObject remove(String key) {

        if(key == null) return null;

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

    /**
     * Copies all values from the other section if they do not exist in this section
     * @param other The section to copy from
     */
    public void fill(ConfigSection other) {
        for(String key : other.orderedKeys) {
            if(!has(key)) {
                set(key, other.getOrThrow(key).copy());
            } else if(hasSection(key) && other.hasSection(key)) {
                getSection(key).fill(other.getSection(key));
            }
        }
    }

    /**
     * Copies all values from the other section
     * @param other The section to copy from
     */
    public void fillOverwrite(ConfigSection other) {
        for(String key : other.orderedKeys) {
            set(key, other.getOrThrow(key).copy());
        }
    }

    /**
     * Determines the number of entries in the Section
     * @return The number of entries in the Section
     */
    public int size() {

        return values.size();
    }

    /**
     * Creates a collection of keys in the Section
     * @return The keys in the section
     */
    public Collection<String> getKeys() {
        return List.copyOf(orderedKeys);
    }

    /**
     * Associates the given value with the given key, overwriting any existing value if necessary, then returns a reference to self
     * @param key The key to associate the given value with
     * @param value The value to put into the section. If null, the key will be removed from the map instead
     * @return A reference to self
     */
    public ConfigSection with(String key, ConfigObject value) {
        set(key, value);
        return this;
    }

    /**
     * Serializes an object, then associates it with the given key, overwriting any existing value if necessary, then returns a reference to self
     * @param key The key to associate the given value with
     * @param value The object to serialize
     * @param serializer The serializer to use to serialize the value
     * @return A reference to self
     * @param <T> The type of object passed in to be serialized
     */
    public <T> ConfigSection with(String key, T value, Serializer<T> serializer) {
        set(key, value, serializer);
        return this;
    }

    /**
     * Associates a given String with the given key, then returns a reference to self
     * @param key The key to associate the given String with
     * @param value The String to put into the section.
     * @return A reference to self
     */
    public ConfigSection with(String key, String value) {
        set(key, value);
        return this;
    }

    /**
     * Associates a given Number with the given key, then returns a reference to self
     * @param key The key to associate the given String with
     * @param value The String to put into the section.
     * @return A reference to self
     */
    public ConfigSection with(String key, Number value) {
        set(key, value);
        return this;
    }

    /**
     * Associates a given Boolean with the given key, then returns a reference to self
     * @param key The key to associate the given String with
     * @param value The String to put into the section.
     * @return A reference to self
     */
    public ConfigSection with(String key, Boolean value) {
        set(key, value);
        return this;
    }

    /**
     * Creates a stream of each key-value pair in the ConfigSection
     * @return A stream of the key-value pairs in the ConfigSection
     */
    public Stream<Tuples.T2<String, ConfigObject>> stream() {

        return orderedKeys.stream().map(key -> new Tuples.T2<>(key, get(key)));
    }

    @Override
    public ConfigSection copy() {

        ConfigSection out = new ConfigSection();
        for(String key : orderedKeys) {
            out.set(key, getOrThrow(key).copy());
        }
        return out;
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
    public boolean isBlob() {
        return false;
    }

    @Override
    public ConfigPrimitive asPrimitive() {
        throw new IllegalStateException("Cannot convert a section to a primitive!");
    }

    @Override
    public ConfigList asList() {
        throw new IllegalStateException("Cannot convert a section to a list!");
    }

    @Override
    public ConfigSection asSection() {
        return this;
    }

    @Override
    public ConfigBlob asBlob() {
        throw new IllegalStateException("Cannot convert a section to a blob!");
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

            if(!Objects.equals(obj, otherObj)) return false;
        }

        return true;
    }

    /**
     * Attempts to construct a ConfigSection by converting the values in the given map to ConfigSection
     * @param map The map to attempt to convert
     * @return A new ConfigSection with the given values
     * @throws IllegalArgumentException If the keys in the map are not Strings or the values cannot be converted to ConfigObjects
     */
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
