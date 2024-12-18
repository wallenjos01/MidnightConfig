package org.wallentines.mdcfg.serializer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

public class DelegatedContext<T, V> implements SerializeContext<T> {

    private final SerializeContext<T> delegate;
    private final V value;

    public DelegatedContext(SerializeContext<T> delegate, V value) {
        this.delegate = delegate;
        this.value = value;
    }

    public V getContextValue() {
        return value;
    }

    @Override
    public SerializeResult<String> asString(T object) {
        return delegate.asString(object);
    }

    @Override
    public SerializeResult<Number> asNumber(T object) {
        return delegate.asNumber(object);
    }

    @Override
    public SerializeResult<Boolean> asBoolean(T object) {
        return delegate.asBoolean(object);
    }

    @Override
    public SerializeResult<ByteBuffer> asBlob(T object) {
        return delegate.asBlob(object);
    }

    @Override
    public SerializeResult<Collection<T>> asList(T object) {
        return delegate.asList(object);
    }

    @Override
    public SerializeResult<Map<String, T>> asMap(T object) {
        return delegate.asMap(object);
    }

    @Override
    public SerializeResult<Map<String, T>> asOrderedMap(T object) {
        return delegate.asOrderedMap(object);
    }

    @Override
    public Type getType(T object) {
        return delegate.getType(object);
    }

    @Override
    public Collection<String> getOrderedKeys(T object) {
        return delegate.getOrderedKeys(object);
    }

    @Override
    public T get(String key, T object) {
        return delegate.get(key, object);
    }

    @Override
    public T toString(String object) {
        return delegate.toString(object);
    }

    @Override
    public T toNumber(Number object) {
        return delegate.toNumber(object);
    }

    @Override
    public T toBoolean(Boolean object) {
        return delegate.toBoolean(object);
    }

    @Override
    public T toBlob(ByteBuffer object) {
        return delegate.toBlob(object);
    }

    @Override
    public T toList(Collection<T> list) {
        return delegate.toList(list);
    }

    @Override
    public T toMap(Map<String, T> map) {
        return delegate.toMap(map);
    }

    @Override
    public T nullValue() {
        return delegate.nullValue();
    }

    @Override
    public T set(String key, T value, T object) {
        return delegate.set(key, value, object);
    }

    @Override
    public boolean supportsMeta(T object) {
        return delegate.supportsMeta(object);
    }

    @Override
    public String getMetaProperty(T object, String key) {
        return delegate.getMetaProperty(object, key);
    }

    @Override
    public void setMetaProperty(T object, String key, String value) {
        delegate.setMetaProperty(object, key, value);
    }
}
