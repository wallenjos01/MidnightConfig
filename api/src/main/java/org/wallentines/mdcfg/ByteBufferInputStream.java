package org.wallentines.mdcfg;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer internal;
    private final int length;

    public ByteBufferInputStream(ByteBuffer internal) {
        this(internal, 0, internal.limit());
    }


    public ByteBufferInputStream(ByteBuffer internal, int pos, int length) {
        this.internal = internal.asReadOnlyBuffer();
        this.internal.rewind();
        this.length = length;
        internal.position(pos);
    }

    @Override
    public int read(byte @NotNull [] data, int off, int len) throws IOException {

        int remaining = available();
        if(remaining == 0) return -1;

        int length = Math.min(remaining, len);
        internal.get(data, 0, length);
        return length;
    }

    @Override
    public int read() throws IOException {
        return internal.get();
    }

    @Override
    public int available() throws IOException {
        return length - internal.position();
    }
}
