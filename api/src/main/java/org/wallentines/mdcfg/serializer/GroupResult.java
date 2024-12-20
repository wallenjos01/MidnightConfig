package org.wallentines.mdcfg.serializer;

import java.util.List;

public class GroupResult {

    private final List<Object> values;
    private int index;

    public GroupResult(List<Object> values) {
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        return (T) values.get(index);
    }

    public <T> T get(int index, Class<T> clazz) {
        Object out = values.get(index);
        if(out == null) return null;
        if(out.getClass() == clazz || clazz.isAssignableFrom(out.getClass())) {
            return clazz.cast(out);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T next() {
        return (T) values.get(index++);
    }

    public <T> T next(Class<T> clazz) {
        Object out = values.get(index);
        if(out == null) return null;
        if(out.getClass() == clazz || clazz.isAssignableFrom(out.getClass())) {
            return clazz.cast(out);
        }
        return null;
    }
}
