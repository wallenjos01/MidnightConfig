package org.wallentines.mdcfg;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReference<T> {

    protected final Type type;

    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getTypeClass() {
        if(type instanceof Class) {
            return (Class<T>) type;
        } else if(type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        throw new IllegalStateException("Cannot determine type class for " + type);
    }

}
