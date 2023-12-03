import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.codec.FileCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;

public class TestBinary {

    @Test
    public void test() {

        ConfigSection section = new ConfigSection()
                .with("String", "String")
                .with("Integer", 21)
                .with("Long", 33L)
                .with("Float", 152.455f)
                .with("List", new ConfigList().append(128).append("String"))
                .with("Section", new ConfigSection().with("Key", "Value"));

        FileCodec codec = BinaryCodec.fileCodec();

        File file = new File("test.mdb");
        try {
            codec.saveToFile(ConfigContext.INSTANCE, section, file, Charset.defaultCharset());
        } catch (IOException ex) {
            Assertions.fail();
        }

        ConfigSection read = null;
        try {
            read = codec.loadFromFile(ConfigContext.INSTANCE, file, Charset.defaultCharset()).asSection();
        } catch (IOException ex) {
            Assertions.fail();
        }

        Assertions.assertEquals(section, read);
    }

    @Test
    public void testLarge() {

        ConfigSection section = new ConfigSection();

        Random rand = new Random();

        for(int i = 0 ; i < 10000 ; i++) {
                section.with("S" + i, "V" + i)
                    .with("I" + i, i)
                    .with("L" + i, rand.nextLong())
                    .with("F" + i, rand.nextFloat())
                    .with("LS" + i, new ConfigList().append(rand.nextInt()).append("S" + i))
                    .with("SN" + i, new ConfigSection().with("K" + i, "V" + i));
        }

        FileCodec codec = BinaryCodec.fileCodec();
        FileCodec codec2 = BinaryCodec.fileCodec(new BinaryCodec(BinaryCodec.Compression.DEFLATE));
        FileCodec codec3 = BinaryCodec.fileCodec(new BinaryCodec(BinaryCodec.Compression.NONE));

        File file = new File("test_large.mdb");
        File file2 = new File("test_large_zip.mdb");
        File file3 = new File("test_large_uncompressed.mdb");

        if(file.exists() && !file.delete()) {
            Assertions.fail("Unable to delete test files!");
        }
        if(file2.exists() && !file2.delete()) {
            Assertions.fail("Unable to delete test files!");
        }
        if(file3.exists() && !file3.delete()) {
            Assertions.fail("Unable to delete test files!");
        }

        try {
            codec.saveToFile(ConfigContext.INSTANCE, section, file, StandardCharsets.UTF_8);
            codec2.saveToFile(ConfigContext.INSTANCE, section, file2, StandardCharsets.UTF_8);
            codec3.saveToFile(ConfigContext.INSTANCE, section, file3, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Assertions.fail();
        }

        try {
            Assertions.assertTrue(Files.size(file.toPath()) < Files.size(file3.toPath()));
            Assertions.assertTrue(Files.size(file2.toPath()) < Files.size(file3.toPath()));
        } catch (IOException ex) {
            Assertions.fail();
        }

        // Read back the data
        try {
            ConfigObject read = codec.loadFromFile(ConfigContext.INSTANCE, file, StandardCharsets.UTF_8);
            Assertions.assertTrue(read.isSection());
            Assertions.assertEquals(read, section);

            read = codec2.loadFromFile(ConfigContext.INSTANCE, file2, StandardCharsets.UTF_8);
            Assertions.assertTrue(read.isSection());
            Assertions.assertEquals(read, section);

            read = codec3.loadFromFile(ConfigContext.INSTANCE, file3, StandardCharsets.UTF_8);
            Assertions.assertTrue(read.isSection());
            Assertions.assertEquals(read, section);

        } catch (Exception ex) {
            Assertions.fail("An exception occurred while reading binary data!", ex);
        }
    }
}
