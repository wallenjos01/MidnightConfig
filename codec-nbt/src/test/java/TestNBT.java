
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.EncodeException;
import org.wallentines.mdcfg.codec.NBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class TestNBT {

    @Test
    public void testHelloWorld() {
        testFile("hello_world.nbt", false);
    }

    @Test
    public void testBig() {
        testFile("bigtest.nbt", true);
    }

    private void testFile(String name, boolean compress) {

        NBTCodec codec = new NBTCodec(true);
        File f = new File(name);
        File fOut = new File(name + ".out");

        Assertions.assertTrue(f.isFile());
        ConfigObject decoded;

        try(FileInputStream fis = new FileInputStream(f)) {

            InputStream is = new BufferedInputStream(compress ? new GZIPInputStream(fis) : fis);
            decoded = codec.decode(ConfigContext.INSTANCE, is, StandardCharsets.UTF_8);

        } catch (Exception ex) {

            Assertions.fail("An error occurred while decoding NBT! File: " + name + ", Compressed: " + compress, ex);
            return;
        }

        Assertions.assertTrue(decoded.isSection());


        try(FileOutputStream fos = new FileOutputStream(fOut)) {

            codec.encode(ConfigContext.INSTANCE, decoded, fos, StandardCharsets.UTF_8);

        } catch (Exception ex) {

            Assertions.fail("An error occurred while encoding NBT! File: " + name + ", Compressed: " + compress, ex);
        }


        ConfigObject reDecoded;
        try(FileInputStream fis = new FileInputStream(fOut)) {
            reDecoded = codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);

        } catch (Exception ex) {

            Assertions.fail("An error occurred while re-decoding NBT! File: " + name + ", Compressed: " + compress, ex);
            return;
        }

        Assertions.assertEquals(decoded, reDecoded);

    }

    @Test
    public void testBooleans() {

        NBTCodec codec = new NBTCodec(false);

        byte[] value;
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            codec.encode(ConfigContext.INSTANCE, new ConfigPrimitive(true), bos);
            value = bos.toByteArray();
        } catch (IOException | EncodeException ex) {
            Assertions.fail("An exception occurred while encoding a boolean!");
            return;
        }

        try(ByteArrayInputStream bis = new ByteArrayInputStream(value)) {

            boolean bl = codec.decode(ConfigContext.INSTANCE, Serializer.BOOLEAN, bis).getOrThrow();
            Assertions.assertTrue(bl);

        } catch(IOException | DecodeException ex) {
            Assertions.fail("An exception occurred while decoding boolean!");
        }

    }

}
