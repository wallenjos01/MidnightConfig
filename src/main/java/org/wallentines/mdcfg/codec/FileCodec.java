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

public class FileCodec {

    private final Codec base;
    private final String defaultExtension;
    private final Set<String> fileExtensions = new HashSet<>();

    public FileCodec(Codec base, String defaultExtension) {
        this(base, defaultExtension, null);
    }

    public FileCodec(Codec base, String defaultExtension, Collection<String> additionalExtensions) {
        this.base = base;
        this.defaultExtension = defaultExtension;
        this.fileExtensions.add(defaultExtension);
        if(additionalExtensions != null) this.fileExtensions.addAll(additionalExtensions);
    }

    public Collection<String> getSupportedExtensions() {
        return fileExtensions;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public <T> T loadFromFile(SerializeContext<T> context, File f, Charset charset) {

        if(!f.exists()) return null;

        try(FileInputStream fis = new FileInputStream(f)) {
            return base.decode(context, fis, charset);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public <T> void saveToFile(SerializeContext<T> context, T data, File f, Charset charset) {

        try(FileOutputStream fos = new FileOutputStream(f)) {
            base.encode(context, data, fos, charset);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "FileCodec{" +
                "defaultExtension='" + defaultExtension + '\'' +
                '}';
    }
}
