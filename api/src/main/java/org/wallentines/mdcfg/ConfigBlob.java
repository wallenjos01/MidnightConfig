package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ConfigBlob extends ConfigObject {

    private final ByteBuffer data;

    public ConfigBlob(ByteBuffer buffer) {
        super(SerializeContext.Type.BLOB);
        this.data = buffer;
    }

    public ConfigBlob(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    public ConfigBlob(ByteArrayOutputStream stream) {
        this(stream.toByteArray());
    }

    public int getSize() {
        return data.position();
    }

    public ByteBuffer getData() {
        return data;
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
        return false;
    }

    @Override
    public boolean isBlob() {
        return true;
    }


    @Override
    public ConfigPrimitive asPrimitive() {
        throw new IllegalStateException("Cannot convert a blob to a primitive!");
    }

    @Override
    public ConfigList asList() {
        throw new IllegalStateException("Cannot convert a blob to a list!");
    }

    @Override
    public ConfigSection asSection() {
        throw new IllegalStateException("Cannot convert a blob to a section!");
    }

    @Override
    public ConfigBlob asBlob() {
        return this;
    }

    @Override
    public ConfigObject copy() {
        return new ConfigBlob(data.duplicate());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigBlob that = (ConfigBlob) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "ConfigBlob{" +
                "data=" + data +
                '}';
    }

}
