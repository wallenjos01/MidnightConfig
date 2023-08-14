package org.wallentines.mdcfg.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A class which associates a file with a codec and serialization context, so data can easily be read from or written to it
 * @param <T> The type of data to read from or write to the file
 */
public class FileWrapper<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger("FileWrapper");

    private final SerializeContext<T> context;
    private final FileCodec codec;
    private final File file;
    private final Charset charset;

    private T root;

    /**
     * Creates a new file wrapper from the given file with the given serialization context and file codec, using UTF-8
     * encoding
     * @param context The context by which to encode/decode data
     * @param codec The codec to use to encode/decode data
     * @param file The file to read from or write to
     */
    public FileWrapper(@NotNull SerializeContext<T> context, @NotNull FileCodec codec, @NotNull File file) {
        this(context, codec, file, StandardCharsets.UTF_8);
    }

    /**
     * Creates a new file wrapper from the given file with the given serialization context and file codec, using the
     * given charset
     * @param context The context by which to encode/decode data
     * @param codec The codec to use to encode/decode data
     * @param file The file to read from or write to
     * @param charset The charset to interpret/write data as
     */
    public FileWrapper(SerializeContext<T> context, FileCodec codec, File file, Charset charset) {
        this.context = context;
        this.file = file;
        this.codec = codec;
        this.charset = charset;
    }

    /**
     * Attempts to load data from the file into the root of the wrapper. If data cannot be read or decoded, nothing
     * will happen
     */
    public void load() {
        try {
            root = codec.loadFromFile(context, file, charset);
        } catch (IOException ex) {
            LOGGER.error("An exception occurred while attempting to read data from file " + file.getAbsolutePath() + "!", ex);
        } catch (DecodeException ex) {
            LOGGER.error("An exception occurred while attempting to decode data from file " + file.getAbsolutePath() + "!", ex);
        }
    }

    /**
     * Attempts to save the root data of the wrapper into the given file. If data cannot be written, nothing will happen
     */
    public void save() {
        try {
            codec.saveToFile(context, root, file, charset);
        } catch (IOException ex) {
            LOGGER.error("An exception occurred while attempting to write data to file " + file.getAbsolutePath() + "!", ex);
        }
    }

    /**
     * Gets the root data in the wrapper
     * @return The root data in the wrapper
     */
    @Nullable
    public T getRoot() {
        return root;
    }

    /**
     * Changes the root data in the wrapper
     * @param newRoot The wrapper's new root
     */
    public void setRoot(@Nullable T newRoot) {
        this.root = newRoot;
    }

    /**
     * Gets the file which this wrapper reads from and writes to
     * @return The wrapped file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the codec used to encode and decode data while reading from and writing to the file
     * @return The codec
     */
    public FileCodec getCodec() {
        return codec;
    }

    /**
     * Gets the charset used to write text to or read text from the file
     * @return The file's charset
     */
    public Charset getCharset() {
        return charset;
    }
}
