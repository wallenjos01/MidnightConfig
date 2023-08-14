import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger("TestFiles");

    @Test
    public void testReadFile() {

        File f = new File("test.json");

        try(FileInputStream fis = new FileInputStream(f)) {

            JSONCodec codec = new JSONCodec();
            ConfigObject obj = codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);
            testLoadedFile(obj);

        } catch (IOException ex) {
            LOGGER.error("An IOException occurred while reading a file!", ex);
            Assertions.fail();
        }
    }

    @Test
    public void testReadEmpty() {

        File f = new File("empty.json");

        try(FileInputStream fis = new FileInputStream(f)) {

            JSONCodec codec = new JSONCodec();
            codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            LOGGER.error("An IOException occurred while reading a file!", ex);
            Assertions.fail();
        } catch (DecodeException ex) {
            // Ignore: This is expected
        }
    }

    @Test
    public void testWriteFile() {

        File f = new File("test_write.json");
        if(f.exists()) Assertions.assertTrue(f.delete());

        JSONCodec codec = JSONCodec.readable();

        ConfigSection section = new ConfigSection()
                .with("String", "Hello")
                .with("Number", 541)
                .with("List", ConfigList.of(55, "Goodbye"))
                .with("Section", new ConfigSection().with("Key", "Value"));

        try(FileOutputStream fos = new FileOutputStream(f)) {

            codec.encode(ConfigContext.INSTANCE, section, fos, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            LOGGER.error("An IOException occurred while writing a file!", ex);
            Assertions.fail();
        }

        try(FileInputStream fis = new FileInputStream(f)) {

            ConfigObject obj = codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);

            Assertions.assertEquals(section, obj);

        } catch (IOException ex) {
            LOGGER.error("An IOException occurred while reading a file!", ex);
            Assertions.fail();
        }
    }

    public static void testLoadedFile(ConfigObject obj) {

        Assertions.assertNotNull(obj);
        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals(5, obj.asSection().size());

        Assertions.assertEquals("Hello", obj.asSection().getString("String"));
        Assertions.assertEquals(45, obj.asSection().getInt("Int"));
        Assertions.assertEquals(111.45345, obj.asSection().getDouble("Float"), 0.001);

        Assertions.assertTrue(obj.asSection().hasList("List"));
        Assertions.assertEquals(4, obj.asSection().getList("List").size());
        Assertions.assertEquals("Wow", obj.asSection().getList("List").get(0).asPrimitive().asString());
        Assertions.assertEquals(1543, obj.asSection().getList("List").get(1).asPrimitive().asInt());

        Assertions.assertTrue(obj.asSection().getList("List").get(2).isList());
        Assertions.assertEquals(1, obj.asSection().getList("List").get(2).asList().size());
        Assertions.assertEquals("Sublist", obj.asSection().getList("List").get(2).asList().get(0).asPrimitive().asString());

        Assertions.assertTrue(obj.asSection().getList("List").get(3).isSection());
        Assertions.assertEquals(1, obj.asSection().getList("List").get(3).asSection().size());
        Assertions.assertEquals("Value", obj.asSection().getList("List").get(3).asSection().getString("Key"));

        Assertions.assertTrue(obj.asSection().hasSection("Object"));
        Assertions.assertEquals(1, obj.asSection().getSection("Object").size());
        Assertions.assertTrue(obj.asSection().getSection("Object").hasSection("Subobject"));
        Assertions.assertEquals(1, obj.asSection().getSection("Object").getSection("Subobject").size());
        Assertions.assertTrue(obj.asSection().getSection("Object").getSection("Subobject").getOrThrow("Value").isPrimitive());
        Assertions.assertTrue(obj.asSection().getSection("Object").getSection("Subobject").getOrThrow("Value").asPrimitive().isString());
        Assertions.assertEquals("!", obj.asSection().getSection("Object").getSection("Subobject").getString("Value"));
    }

}
