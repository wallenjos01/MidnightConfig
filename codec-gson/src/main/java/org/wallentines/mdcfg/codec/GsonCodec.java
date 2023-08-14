package org.wallentines.mdcfg.codec;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.charset.Charset;

public class GsonCodec implements Codec {

    private static final Logger LOGGER = LoggerFactory.getLogger("GsonCodec");
    private final Gson gson;

    public GsonCodec(Gson gson) {
        this.gson = gson;
    }

    public static GsonCodec minified() {
        return new GsonCodec(new GsonBuilder().disableHtmlEscaping().create());
    }

    public static GsonCodec readable() {
        return new GsonCodec(new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create());
    }

    public static FileCodec fileCodec() {
        return fileCodec(readable());
    }

    public static FileCodec fileCodec(GsonCodec codec) {
        return new FileCodec(codec, "json");
    }

    @Override
    public <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset) {

        try(Writer writer = new OutputStreamWriter(stream, charset)) {
            gson.toJson(context.convert(GsonContext.INSTANCE, input), writer);
        } catch (IOException ex) {
            LOGGER.error("An exception occurred while writing Gson to a stream!", ex);
        }
    }

    @Override
    public <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset) throws DecodeException {

        try(Reader reader = new InputStreamReader(stream, charset)) {
            JsonElement ele = gson.fromJson(reader, JsonElement.class);
            return GsonContext.INSTANCE.convert(context, ele);

        } catch (IOException | JsonSyntaxException ex) {

            throw new DecodeException("An error occurred while reading Gson from a stream! " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
