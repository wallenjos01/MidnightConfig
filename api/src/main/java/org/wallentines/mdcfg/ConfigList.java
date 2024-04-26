package org.wallentines.mdcfg;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeException;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ConfigList extends ConfigObject {

    private final List<ConfigObject> values;

    private ConfigList(Collection<ConfigObject> values) {
        super(SerializeContext.Type.LIST);
        this.values = List.copyOf(values);
    }

    /**
     * Creates an empty ConfigList
     */
    public ConfigList() {
        super(SerializeContext.Type.LIST);
        this.values = new ArrayList<>();
    }

    /**
     * Adds a value to the list
     * @param value The value to add
     * @return Whether the value was successfully added
     */
    public boolean add(ConfigObject value) {
        return values.add(value);
    }

    /**
     * Serializes a value, then adds it to the list
     * @param value The value to serialize
     * @param serializer The serializer to use to serialize the given value
     * @return Whether the value was successfully added
     * @param <T> The type of object the serializer will create
     */
    public <T> boolean add(T value, @NotNull Serializer<T> serializer) {

        Optional<ConfigObject> serialized = serializer.serialize(ConfigContext.INSTANCE, value).get();
        return serialized.filter(values::add).isPresent();
    }

    /**
     * Adds a String to the list
     * @param value The String to add
     * @return Whether the String was successfully added
     */
    public boolean add(String value) {
        return values.add(new ConfigPrimitive(value));
    }

    /**
     * Adds a Number to the list
     * @param value The Number to add
     * @return Whether the Number was successfully added
     */
    public boolean add(Number value) {
        return values.add(new ConfigPrimitive(value));
    }

    /**
     * Adds a Boolean to the list
     * @param value The Boolean to add
     * @return Whether the Boolean was successfully added
     */
    public boolean add(Boolean value) {
        return values.add(new ConfigPrimitive(value));
    }

    /**
     * Adds a value to the list, returning a reference to self
     * @param value The value to add
     * @return A reference to self
     * @throws IllegalArgumentException If the value could not be added
     */
    public ConfigList append(ConfigObject value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    /**
     * Adds a String to the list, returning a reference to self
     * @param value The String to add
     * @return A reference to self
     * @throws IllegalArgumentException If the value could not be added
     */
    public ConfigList append(String value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    /**
     * Adds a Number to the list, returning a reference to self
     * @param value The Number to add
     * @return A reference to self
     * @throws IllegalArgumentException If the value could not be added
     */
    public ConfigList append(Number value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    /**
     * Adds a Boolean to the list, returning a reference to self
     * @param value The Boolean to add
     * @return A reference to self
     * @throws IllegalArgumentException If the value could not be added
     */
    public ConfigList append(Boolean value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    /**
     * Serializes a value, then adds it to the list, returning a reference to self
     * @param value The value to serialize
     * @param serializer The serializer to use to serialize the given value
     * @return A reference to self
     * @param <T> The type of object the serializer will create
     */
    public <T> ConfigList append(T value, @NotNull Serializer<T> serializer) {

        Optional<ConfigObject> serialized = serializer.serialize(ConfigContext.INSTANCE, value).get();
        serialized.ifPresent(this::add);
        return this;
    }

    /**
     * Adds a group of objects to the list
     * @param objects The objects to add
     * @throws IllegalArgumentException If any of the values could not be added
     */
    public void addAll(Collection<ConfigObject> objects) {
        objects.forEach(this::append);
    }

    /**
     * Adds a group of objects to the list
     * @param objects The objects to add
     * @return A reference to self
     * @throws IllegalArgumentException If any of the values could not be added
     */
    public ConfigList appendAll(Collection<ConfigObject> objects) {
        objects.forEach(this::append);
        return this;
    }

    /**
     * Determines the number of items in the list
     * @return The number of items in the list
     */
    public int size() {
        return values.size();
    }

    /**
     * Gets a reference to the object at the given index
     * @param index The index to lookup
     * @return The value at the given index
     * @throws IndexOutOfBoundsException If there is no value at the given index
     */
    public ConfigObject get(int index) {
        return values.get(index);
    }

    /**
     * Deserializes a value at the given index, then returns it
     * @param index The index to lookup
     * @param serializer The serializer to use to deserialize the given value
     * @return The deserialized value at the given index
     * @param <T> The type of object the serializer will parse
     * @throws IndexOutOfBoundsException If there is no value at the given index
     * @throws SerializeException If the value at the given index could not be deserialized
     */
    public <T> T get(int index, @NotNull Serializer<T> serializer) {

        return serializer.deserialize(ConfigContext.INSTANCE, values.get(index)).getOrThrow();
    }

    /**
     * Determines whether the list contains any values equivalent to the given object
     * @param object The object to search for
     * @return Whether the list contains any values equivalent to the given object
     */
    public boolean contains(ConfigObject object) {
        return values.contains(object);
    }

    /**
     * Determines whether the list contains any values equivalent to the given String
     * @param object The String to search for
     * @return Whether the list contains any values equivalent to the given String
     */
    public boolean contains(String object) {
        return values.contains(new ConfigPrimitive(object));
    }

    /**
     * Determines whether the list contains any values equivalent to the given Number
     * @param object The String to search for
     * @return Whether the list contains any values equivalent to the given Number
     */
    public boolean contains(Number object) {
        return values.contains(new ConfigPrimitive(object));
    }

    /**
     * Determines whether the list contains any values equivalent to the given Boolean
     * @param object The String to search for
     * @return Whether the list contains any values equivalent to the given Boolean
     */
    public boolean contains(Boolean object) {
        return values.contains(new ConfigPrimitive(object));
    }

    /**
     * Removes all items from the list
     */
    public void clear() {
        values.clear();
    }

    /**
     * Removes the value at the given index
     * @param index The index to lookup and remove
     * @throws IndexOutOfBoundsException If there is no object at the given index
     */
    public void remove(int index) {
        values.remove(index);
    }

    /**
     * Removes the given value from the list
     * @param value The value to lookup and remove
     * @return true if the item was present and was removed
     */
    public boolean remove(ConfigObject value) {
        return values.remove(value);
    }

    /**
     * Returns a collection of the values in the list
     * @return A collection of values in the list
     */
    public Collection<ConfigObject> values() {
        return values;
    }

    /**
     * Creates a stream of the values in the list
     * @return A stream of values in the list
     */
    public Stream<ConfigObject> stream() {
        return values.stream();
    }

    /**
     * Attempts to create a ConfigList by converting each of the given objects into ConfigObjects
     * @param objs A collection of objects to convert and add to the list
     * @return A new ConfigList with the given objects converted to ConfigObjects
     * @throws IllegalArgumentException If any of the objects cannot be converted to a ConfigObject
     */
    public static ConfigList of(Collection<?> objs) {
        ConfigList out = new ConfigList();
        for(Object o : objs) {
            out.add(ConfigObject.toConfigObject(o));
        }
        return out;
    }

    /**
     * Attempts to create a ConfigList by converting each of the given objects into ConfigObjects
     * @param objs The objects to convert and add to the list
     * @return A new ConfigList with the given objects converted to ConfigObjects
     * @throws IllegalArgumentException If any of the objects cannot be converted to a ConfigObject
     */
    public static ConfigList of(Object... objs) {

        ConfigList out = new ConfigList();
        for(Object o : objs) {
            out.add(ConfigObject.toConfigObject(o));
        }
        return out;
    }

    /**
     * Creates a ConfigList using the given values
     * @param collection The objects to and add to the list
     * @return A new ConfigList with the given ConfigObjects
     */
    public static ConfigList create(Collection<ConfigObject> collection) {

        ConfigList out = new ConfigList();
        out.values.addAll(collection);
        return out;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @Override
    public boolean isBlob() {
        return false;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public ConfigPrimitive asPrimitive() {
        throw new IllegalStateException("Cannot convert a list to a primitive!");
    }

    @Override
    public ConfigList asList() {
        return this;
    }

    @Override
    public ConfigSection asSection() {
        throw new IllegalStateException("Cannot convert a list to a section!");
    }

    @Override
    public ConfigBlob asBlob() {
        throw new IllegalStateException("Cannot convert a list to a blob!");
    }

    @Override
    public ConfigList copy() {
        ConfigList out = new ConfigList();
        for(ConfigObject obj : values) {
            out.add(obj.copy());
        }
        return out;
    }

    @Override
    public ConfigList freeze() {
        return new Frozen(this);
    }

    @Override
    public String toString() {
        return "ConfigList{" +
                "size=" + size() +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ConfigList otherList = (ConfigList) other;

        if(values.size() != otherList.values.size()) return false;

        for(int i = 0 ; i < values.size() ; i++) {
            if(!values.get(i).equals(otherList.values.get(i))) return false;
        }

        return true;
    }

    /**
     * Frozen ConfigList
     */
    private static class Frozen extends ConfigList {

        static UnsupportedOperationException exception() { return new UnsupportedOperationException("ConfigList is frozen!"); }

        private Frozen(ConfigList list) {
            super(list.values.stream().map(ConfigObject::freeze).toList());
        }

        @Override
        public boolean add(ConfigObject value) {
            throw exception();
        }

        @Override
        public <T> boolean add(T value, @NotNull Serializer<T> serializer) {
            throw exception();
        }

        @Override
        public boolean add(String value) {
            throw exception();
        }

        @Override
        public boolean add(Number value) {
            throw exception();
        }

        @Override
        public boolean add(Boolean value) {
            throw exception();
        }

        @Override
        public ConfigList append(ConfigObject value) {
            return copy().append(value);
        }

        @Override
        public ConfigList append(String value) {
            return copy().append(value);
        }

        @Override
        public ConfigList append(Number value) {
            return copy().append(value);
        }

        @Override
        public ConfigList append(Boolean value) {
            return copy().append(value);
        }

        @Override
        public <T> ConfigList append(T value, @NotNull Serializer<T> serializer) {
            return copy().append(value, serializer);
        }

        @Override
        public void addAll(Collection<ConfigObject> objects) {
            throw exception();
        }

        @Override
        public ConfigList appendAll(Collection<ConfigObject> objects) {
            return copy().appendAll(objects);
        }

        @Override
        public void clear() {
            throw exception();
        }

        @Override
        public void remove(int index) {
            throw exception();
        }

        @Override
        public boolean remove(ConfigObject value) {
            throw exception();
        }
    }

}
