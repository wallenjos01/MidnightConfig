package org.wallentines.mdcfg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ConfigBlob extends ConfigObject {

    private final ByteBuffer data;
    public ConfigBlob(byte[] data) {
        this.data = ByteBuffer.wrap(data);
    }

    public ConfigBlob(ByteBuffer buffer) {
        this.data = buffer;
    }

    public ConfigBlob(ByteArrayOutputStream stream) {
        this.data = ByteBuffer.wrap(stream.toByteArray());
    }

    public int getSize() {
        return data.capacity();
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
}
