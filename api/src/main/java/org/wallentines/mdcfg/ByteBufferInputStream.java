package org.wallentines.mdcfg;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer internal;
    private final int length;

    public ByteBufferInputStream(ByteBuffer internal) {
        this.internal = internal;
        this.length = internal.position();
        internal.position(0);
    }

    @Override
    public int read(byte @NotNull [] data, int off, int len) throws IOException {

        int remaining = length - internal.position();
        int length = Math.min(remaining, len);
        internal.get(data, 0, length);
        return length;
    }

    @Override
    public int read() throws IOException {
        return internal.get() & 0xFF;
    }
}
