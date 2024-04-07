package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ConfigBlob extends ConfigObject {

    private final ByteBuffer data;

    public ConfigBlob(ByteBuffer buffer) {
        super(SerializeContext.Type.BLOB);
        this.data = buffer.duplicate();
        data.rewind();
    }

    public ConfigBlob(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    public ConfigBlob(ByteArrayOutputStream stream) {
        this(stream.toByteArray());
    }

    public int getSize() {
        return data.limit();
    }

    public ByteBuffer getData() {
        return data;
    }

    public ByteBufferInputStream asStream() {
        return new ByteBufferInputStream(data);
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
    public ConfigBlob copy() {
        try {
            return ConfigBlob.read(asStream(), getSize());
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

    public static ConfigBlob read(InputStream stream) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] copyBuffer = new byte[1024];
        int read;
        while((read = stream.read(copyBuffer)) > 0) {
            bos.write(copyBuffer, 0, read);
        }

        return new ConfigBlob(bos);
    }

    public static ConfigBlob read(InputStream stream, int length) throws IOException {

        int remaining = length;
        ByteBuffer temp = ByteBuffer.allocate(remaining);

        byte[] copyBuffer = new byte[1024];
        while(remaining > 0) {
            int read = stream.read(copyBuffer);
            if(read == -1) {
                throw new EOFException("Found EOF while reading from a stream!");
            }
            remaining -= read;
            temp.put(copyBuffer, 0, read);
        }

        return new ConfigBlob(temp);
    }

}
