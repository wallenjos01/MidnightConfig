package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public ConfigBlob(InputStream stream) throws IOException {

        super(SerializeContext.Type.BLOB);
        int remaining = stream.available();
        this.data = ByteBuffer.allocate(remaining);

        byte[] copyBuffer = new byte[1024];
        while(remaining > 0) {
            int read = stream.read(copyBuffer);
            remaining -= read;
            this.data.put(copyBuffer, 0, read);
        }
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

    public ByteBufferInputStream asStream() {
        return new ByteBufferInputStream(data.asReadOnlyBuffer());
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
        try {
            return new ConfigBlob(asStream());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to copy blob!", ex);
        }
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
