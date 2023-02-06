import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

public class TestJSON {

    @Test
    public void testEncode() {

        ConfigSection test = new ConfigSection();
        test.set("String", "Hello, World");
        test.set("Number", 42);
        test.set("List", ConfigList.of("String", 69, 112.4));
        test.set("Section", new ConfigSection().with("Hello", "World"));

        // Non-indented
        JSONCodec codec = JSONCodec.minified();
        Assertions.assertEquals("{\"String\":\"Hello, World\",\"Number\":42,\"List\":[\"String\",69,112.4],\"Section\":{\"Hello\":\"World\"}}", codec.encodeToString(ConfigContext.INSTANCE, test));

        // Indented
        codec = JSONCodec.readable();
        Assertions.assertEquals("{\n    \"String\": \"Hello, World\",\n    \"Number\": 42,\n    \"List\": [\n        \"String\",\n        69,\n        112.4\n    ],\n    \"Section\": {\n        \"Hello\": \"World\"\n    }\n}", codec.encodeToString(ConfigContext.INSTANCE, test));

    }

    @Test
    public void testDecode() {

        JSONCodec codec = new JSONCodec();

        String encodedPrimitive = "\"Hello\"";
        ConfigObject primitive = codec.decode(ConfigContext.INSTANCE, encodedPrimitive);

        Assertions.assertTrue(primitive.isPrimitive());
        Assertions.assertTrue(primitive.asPrimitive().isString());
        Assertions.assertEquals("Hello", primitive.asPrimitive().asString());

        String encodedNumber = "1000";
        ConfigObject number = codec.decode(ConfigContext.INSTANCE, encodedNumber);

        Assertions.assertTrue(number.isPrimitive());
        Assertions.assertTrue(number.asPrimitive().isNumber());
        Assertions.assertEquals(1000, number.asPrimitive().asNumber());


        String encodedList = "[111,43.435,\"String\"]";
        ConfigObject list = codec.decode(ConfigContext.INSTANCE, encodedList);

        Assertions.assertTrue(list.isList());
        Assertions.assertEquals(3, list.asList().size());
        Assertions.assertEquals(111, list.asList().get(0).asPrimitive().asInt());
        Assertions.assertEquals(43.435, list.asList().get(1).asPrimitive().asDouble());
        Assertions.assertEquals("String", list.asList().get(2).asPrimitive().asString());

        String encodedObject = "{   \"String\":\n\"Hello, World\",      \"Number\":42,  \"Float\":112.45,  \"List\":[123,321,\"Wow\"\n],\"Object\":{\"Wow\":12}}";

        ConfigObject obj = codec.decode(ConfigContext.INSTANCE, encodedObject);

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Hello, World", obj.asSection().getString("String"));
        Assertions.assertEquals(42, obj.asSection().getInt("Number"));
        Assertions.assertEquals(112.45, obj.asSection().getDouble("Float"));
        Assertions.assertEquals(3, obj.asSection().getList("List").size());
        Assertions.assertEquals(123, obj.asSection().getList("List").get(0).asPrimitive().asInt());
        Assertions.assertEquals(321, obj.asSection().getList("List").get(1).asPrimitive().asInt());
        Assertions.assertEquals("Wow", obj.asSection().getList("List").get(2).asPrimitive().asString());
        Assertions.assertEquals(1, obj.asSection().getSection("Object").size());
        Assertions.assertEquals(12, obj.asSection().getSection("Object").getInt("Wow"));
    }

    @Test
    public void testReadFile() {

        File f = new File("test.json");

        try(FileInputStream fis = new FileInputStream(f)) {

            JSONCodec codec = new JSONCodec();
            ConfigObject obj = codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);
            testLoadedFile(obj);

        } catch (IOException ex) {
            Assertions.fail();
            ex.printStackTrace();
        }
    }

    @Test
    public void testReadEmpty() {

        File f = new File("empty.json");

        try(FileInputStream fis = new FileInputStream(f)) {

            JSONCodec codec = new JSONCodec();
            codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            Assertions.fail();
            ex.printStackTrace();
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
            ex.printStackTrace();
            Assertions.fail();
        }

        try(FileInputStream fis = new FileInputStream(f)) {

            ConfigObject obj = codec.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);

            Assertions.assertEquals(section, obj);

        } catch (IOException ex) {
            ex.printStackTrace();
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

        Assertions.assertTrue(obj.asSection().get("List").isList());
        Assertions.assertEquals(4, obj.asSection().getList("List").size());
        Assertions.assertEquals("Wow", obj.asSection().getList("List").get(0).asPrimitive().asString());
        Assertions.assertEquals(1543, obj.asSection().getList("List").get(1).asPrimitive().asInt());

        Assertions.assertTrue(obj.asSection().getList("List").get(2).isList());
        Assertions.assertEquals(1, obj.asSection().getList("List").get(2).asList().size());
        Assertions.assertEquals("Sublist", obj.asSection().getList("List").get(2).asList().get(0).asPrimitive().asString());

        Assertions.assertTrue(obj.asSection().getList("List").get(3).isSection());
        Assertions.assertEquals(1, obj.asSection().getList("List").get(3).asSection().size());
        Assertions.assertEquals("Value", obj.asSection().getList("List").get(3).asSection().getString("Key"));

        Assertions.assertTrue(obj.asSection().get("Object").isSection());
        Assertions.assertEquals(1, obj.asSection().getSection("Object").size());
        Assertions.assertTrue(obj.asSection().getSection("Object").get("Subobject").isSection());
        Assertions.assertEquals(1, obj.asSection().getSection("Object").getSection("Subobject").size());
        Assertions.assertTrue(obj.asSection().getSection("Object").getSection("Subobject").get("Value").isPrimitive());
        Assertions.assertTrue(obj.asSection().getSection("Object").getSection("Subobject").get("Value").asPrimitive().isString());
        Assertions.assertEquals("!", obj.asSection().getSection("Object").getSection("Subobject").getString("Value"));
    }

    @Test
    public void testQuotes() {

        ConfigSection sec = new ConfigSection().with("Key", "\"Quoted String\"");
        String encoded = JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, sec);
        Assertions.assertEquals("{\"Key\":\"\\\"Quoted String\\\"\"}", encoded);

        ConfigObject obj = JSONCodec.loadConfig(encoded);
        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("\"Quoted String\"", obj.asSection().getString("Key"));

    }

}
