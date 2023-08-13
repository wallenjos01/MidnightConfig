package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Maps file extensions to file codecs so different types of files can be loaded according to their perceived type.
 */
@SuppressWarnings("unused")
public class FileCodecRegistry {

    private FileCodec defaultCodec;
    private final HashMap<String, FileCodec> codecs = new HashMap<>();

    /**
     * Registers a file codec to the registry. If there is no default codec, this will be automatically registered as
     * the default
     * @param codec The codec to register
     */
    public void registerFileCodec(FileCodec codec) {
        registerFileCodec(codec, defaultCodec == null);
    }

    /**
     * Registers a file codec to the registry and optionally sets it as the default
     * @param codec The codec to register
     * @param setAsDefault Whether the codec should be set as the default codec
     */
    public void registerFileCodec(FileCodec codec, boolean setAsDefault) {

        if(setAsDefault) {
            this.defaultCodec = codec;
        }

        for(String extension : codec.getSupportedExtensions()) {
            this.codecs.put(extension, codec);
        }
    }

    /**
     * Determines which codec should be used for the given file based on its extension
     * @param file The file to inspect
     * @return A codec which supports the file's extension, or null if none of them do
     */
    public FileCodec forFile(File file) {

        String name = file.getName();
        int index = name.lastIndexOf('.');
        if(index == -1) return null;

        String ext = name.substring(index + 1);
        return codecs.get(ext);
    }

    /**
     * Determines which codec should be used for the given file extension
     * @param ext The extension to look up
     * @return A codec which supports the file extension, or null if none of them do
     */
    public FileCodec forFileExtension(String ext) {
        return codecs.get(ext);
    }

    /**
     * Creates a file wrapper for the given file using the given context and UTF-8 encoding
     * @param context The context by which to decode/encode data
     * @param file The file to read or read/write
     * @return A new file wrapper
     * @param <T> The type of values to read
     */
    public <T> FileWrapper<T> fromFile(SerializeContext<T> context, File file) {
        return fromFile(context, file, StandardCharsets.UTF_8);
    }

    /**
     * Creates a file wrapper for the given file using the given charset and context
     * @param context The context by which to decode/encode data
     * @param file The file to read or read/write
     * @param charset The charset to interpret/write data as
     * @return A new file wrapper
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> fromFile(SerializeContext<T> context, File file, Charset charset) {

        FileCodec codec = forFile(file);
        if(codec == null) return null;

        FileWrapper<T> wrapper = new FileWrapper<>(context, codec, file, charset);
        wrapper.load();

        return wrapper;
    }

    /**
     * Finds a file with the given prefix in the given folder, and creates a wrapper for it with the given context and
     * UTF-8 encoding
     * @param context The context by which to decode/encode data
     * @param prefix The file's name without the extension (i.e. "data.json" becomes "data")
     * @param folder The folder to search in
     * @return A wrapper for the found file, or null if none is found
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> find(SerializeContext<T> context, String prefix, File folder) {
        return find(context, prefix, folder, StandardCharsets.UTF_8);
    }

    /**
     * Finds a file with the given prefix in the given folder, and creates a wrapper for it with the given charset and
     * context
     * @param context The context by which to decode/encode data
     * @param prefix The file's name without the extension (i.e. "data.json" becomes "data")
     * @param folder The folder to search in
     * @param charset The charset to interpret/write data as
     * @return A wrapper for the found file, or null if none is found
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> find(SerializeContext<T> context, String prefix, File folder, Charset charset) {

        File[] fs = folder.listFiles();
        if(fs != null) for(File file : fs) {

            String name = file.getName();
            int index = name.lastIndexOf('.');
            if(index == -1) continue;

            String fileName = name.substring(0, index);
            String extension = name.substring(index + 1);

            FileCodec codec = forFileExtension(extension);

            if(fileName.equals(prefix) && codec != null) {
                FileWrapper<T> out = new FileWrapper<>(context, codec, file, charset);
                out.load();

                return out;
            }
        }

        return null;
    }

    /**
     * Finds a file with the given prefix in the given folder, or creates one with the default extension, and creates a
     * wrapper for it with the given context and UTF-8 encoding
     * @param context The context by which to decode/encode data
     * @param prefix The file's name without the extension (i.e. "data.json" becomes "data")
     * @param folder The folder to search in
     * @return A wrapper for the found file
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> findOrCreate(SerializeContext<T> context, String prefix, File folder) {
        return findOrCreate(context, prefix, folder, StandardCharsets.UTF_8);
    }

    /**
     * Finds a file with the given prefix in the given folder, or creates one with the default extension, and creates a
     * wrapper for it with the given charset and context
     * @param context The context by which to decode/encode data
     * @param prefix The file's name without the extension (i.e. "data.json" becomes "data")
     * @param folder The folder to search in
     * @param charset The charset to interpret/write data as
     * @return A wrapper for the found file
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> findOrCreate(SerializeContext<T> context, String prefix, File folder, Charset charset) {
        return findOrCreate(context, prefix, folder, charset, null);
    }

    /**
     * Finds a file with the given prefix in the given folder, or creates one with the default extension, and creates a
     * wrapper for it with the given context and UTF-8 encoding, then merges the found data with the given defaults
     * @param context The context by which to decode/encode data
     * @param prefix The file's name without the extension (i.e. "data.json" becomes "data")
     * @param folder The folder to search in
     * @return A wrapper for the found file
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> findOrCreate(SerializeContext<T> context, String prefix, File folder, T defaults) {
        return findOrCreate(context, prefix, folder, StandardCharsets.UTF_8, defaults);
    }

    /**
     * Finds a file with the given prefix in the given folder, or creates one with the default extension, and creates a
     * wrapper for it with the given charset and context, then merges the found data with the given defaults
     * @param context The context by which to decode/encode data
     * @param prefix The file's name without the extension (i.e. "data.json" becomes "data")
     * @param folder The folder to search in
     * @param charset The charset to interpret/write data as
     * @return A wrapper for the found file
     * @param <T> The type of values to read/write
     */
    public <T> FileWrapper<T> findOrCreate(SerializeContext<T> context, String prefix, File folder, Charset charset, T defaults) {

        if(!folder.isDirectory()) throw new IllegalArgumentException("Attempt to find or create a file in non-directory " + folder + "!");
        FileWrapper<T> out = find(context, prefix, folder, charset);
        if(out == null) {

            FileCodec codec = defaultCodec;

            String newFileName = prefix + "." + codec.getDefaultExtension();
            File newFile = new File(folder, newFileName);

            out = new FileWrapper<>(context, codec, newFile, charset);
        }

        if(defaults != null) {
            out.setRoot(context.merge(out.getRoot(), defaults));
        }
        return out;
    }

}
