import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.*;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class TestSNBT {

    @Test
    public void testHelloWorld() {
        testFile("hello_world", false);
    }

    @Test
    public void testBig() {
        testFile("bigtest", true);
    }

    private void testFile(String name, boolean compress) {

        NBTCodec codec = new NBTCodec(true);
        SNBTCodec out = new SNBTCodec(true);
        File f = new File(name + ".nbt");
        File fOut = new File(name + ".snbt");

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

            out.encode(ConfigContext.INSTANCE, decoded, fos, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            Assertions.fail("An error occurred while encoding NBT! File: " + name + ", Compressed: " + compress, ex);
        }


        ConfigObject reDecoded;
        try(FileInputStream fis = new FileInputStream(fOut)) {
            reDecoded = out.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Assertions.fail("An error occurred while re-decoding NBT! File: " + name + ", Compressed: " + compress, ex);
            return;
        }

        Assertions.assertEquals(decoded, reDecoded);
    }

}
