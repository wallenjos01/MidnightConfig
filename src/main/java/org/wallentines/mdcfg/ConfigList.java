package org.wallentines.mdcfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ConfigList implements ConfigObject {

    private final ArrayList<ConfigObject> values = new ArrayList<>();

    public ConfigList() { }

    public ConfigList(Collection<ConfigObject> collection) {
        values.addAll(collection);
    }

    public boolean add(ConfigObject value) {
        return values.add(value);
    }

    public boolean add(String value) {
        return values.add(new ConfigPrimitive(value));
    }

    public boolean add(Number value) {
        return values.add(new ConfigPrimitive(value));
    }

    public boolean add(Boolean value) {
        return values.add(new ConfigPrimitive(value));
    }

    public ConfigList append(ConfigObject value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    public ConfigList append(String value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    public ConfigList append(Number value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    public ConfigList append(Boolean value) {
        if(!add(value)) throw new IllegalArgumentException("Unable to add " + value + " to a list!");
        return this;
    }

    public int size() {
        return values.size();
    }

    public ConfigObject get(int index) {
        return values.get(index);
    }

    public void clear() {
        values.clear();
    }

    public void remove(int index) {
        values.remove(index);
    }

    public void remove(ConfigObject value) {
        values.remove(value);
    }

    public Collection<ConfigObject> values() {
        return values;
    }

    public Stream<ConfigObject> stream() {
        return values.stream();
    }

    public static ConfigList of(Collection<?> objs) {
        ConfigList out = new ConfigList();
        for(Object o : objs) {
            out.add(ConfigObject.toConfigObject(o));
        }
        return out;
    }

    public static ConfigList of(Object... objs) {

        ConfigList out = new ConfigList();
        for(Object o : objs) {
            out.add(ConfigObject.toConfigObject(o));
        }
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
}
