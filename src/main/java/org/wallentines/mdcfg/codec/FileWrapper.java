package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileWrapper<T> {

    private final SerializeContext<T> context;
    private final FileCodec codec;
    private final File file;
    private final Charset charset;

    private T root;

    public FileWrapper(SerializeContext<T> context, FileCodec codec, File file) {
        this(context, codec, file, StandardCharsets.UTF_8);
    }

    public FileWrapper(SerializeContext<T> context, FileCodec codec, File file, Charset charset) {
        this.context = context;
        this.file = file;
        this.codec = codec;
        this.charset = charset;
    }

    public void load() {
        root = codec.loadFromFile(context, file, charset);
    }

    public void save() {
        codec.saveToFile(context, root, file, charset);
    }

    public T getRoot() {
        return root;
    }

    public void setRoot(T newRoot) {
        this.root = newRoot;
    }

    public File getFile() {
        return file;
    }
}
