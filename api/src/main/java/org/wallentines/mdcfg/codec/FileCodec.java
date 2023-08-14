package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains data pertaining to saving data to a file
 */
public class FileCodec {

    private final Codec base;
    private final String defaultExtension;
    private final Set<String> fileExtensions = new HashSet<>();

    /**
     * Constructs a FileCodec with the given base Codec and file extension
     * @param base The codec to use when encoding and decoding files of this type
     * @param defaultExtension The primary file extension for this data type
     */
    public FileCodec(Codec base, String defaultExtension) {
        this(base, defaultExtension, null);
    }

    /**
     * Constructs a FileCodec with the given base Codec and file extensions
     * @param base The codec to use when encoding and decoding files of this type
     * @param defaultExtension The primary file extension for this data type
     * @param additionalExtensions Additional extensions which are valid for this data type
     */
    public FileCodec(Codec base, String defaultExtension, Collection<String> additionalExtensions) {
        this.base = base;
        this.defaultExtension = defaultExtension;
        this.fileExtensions.add(defaultExtension);
        if(additionalExtensions != null) this.fileExtensions.addAll(additionalExtensions);
    }

    /**
     * Gets all valid file extensions for this data type
     * @return All valid file extensions for this data type
     */
    public Collection<String> getSupportedExtensions() {
        return fileExtensions;
    }

    /**
     * Gets the default file extension for this data type
     * @return The default file extension
     */
    public String getDefaultExtension() {
        return defaultExtension;
    }

    /**
     * Decodes data read from a file according to the given context
     * @param context The context by which to decode
     * @param file The file to read
     * @param charset The charset to interpret the file data as
     * @return A decoded value
     * @param <T> The type of values to decode
     */
    public <T> T loadFromFile(SerializeContext<T> context, File file, Charset charset) throws IOException, DecodeException {

        if(!file.exists()) return null;

        try(FileInputStream fis = new FileInputStream(file)) {
            return base.decode(context, fis, charset);
        }
    }

    /**
     * Encodes the given data according to the given context and writes it to the given file
     * @param context The context by which to encode
     * @param data The data to encode
     * @param file The file to write to
     * @param charset The charset to save the data as
     * @param <T> The type of values to encode
     */
    public <T> void saveToFile(SerializeContext<T> context, T data, File file, Charset charset) throws IOException {

        try(FileOutputStream fos = new FileOutputStream(file)) {
            base.encode(context, data, fos, charset);
        }
    }

    @Override
    public String toString() {
        return "FileCodec{" +
                "defaultExtension='" + defaultExtension + '\'' +
                '}';
    }
}
