package org.wallentines.mdcfg.codec;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.charset.Charset;

public class GsonCodec implements Codec {
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
    public <T> void encode(@NotNull SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws EncodeException, IOException{

        try(Writer writer = new OutputStreamWriter(stream, charset)) {
            gson.toJson(context.convert(GsonContext.INSTANCE, input), writer);
        }
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) throws DecodeException, IOException {

        try(Reader reader = new InputStreamReader(stream, charset)) {
            JsonElement ele = gson.fromJson(reader, JsonElement.class);
            return GsonContext.INSTANCE.convert(context, ele);

        } catch (JsonSyntaxException ex) {

            throw new DecodeException("An error occurred while reading Gson from a stream! Invalid syntax: " + ex.getMessage());
        }
    }
}
