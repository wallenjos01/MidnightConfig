import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.File;
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

        String encodedObject = "{   \"String\":\n\"Hello, World\" ,      \"Number\":42,  \"Float\":112.45  \n,  \"List\":[123,321,\"Wow\"\n],\"Object\":{\"Wow\":12}}";

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
    public void testEmpty() {

        String encoded = "{\"Key\":\"\"}";

        ConfigObject decoded = JSONCodec.loadConfig(encoded);
        Assertions.assertTrue(decoded.isSection());

        Assertions.assertEquals("", decoded.asSection().getString("Key"));

    }

    @Test
    public void testQuotes() {

        // Quoted Values
        ConfigSection sec = new ConfigSection().with("Key", "\"Quoted String\"");
        String encoded = JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, sec);
        Assertions.assertEquals("{\"Key\":\"\\\"Quoted String\\\"\"}", encoded);

        ConfigObject obj = JSONCodec.loadConfig(encoded);
        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("\"Quoted String\"", obj.asSection().getString("Key"));

        // Quoted Keys
        sec = new ConfigSection().with("\"Quoted Key\"", "Value");
        encoded = JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, sec);
        Assertions.assertEquals("{\"\\\"Quoted Key\\\"\":\"Value\"}", encoded);

        obj = JSONCodec.loadConfig(encoded);
        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Value", obj.asSection().getString("\"Quoted Key\""));

    }

    @Test
    public void testEscape() {

        String json = "{\"key\":\"Value\\nNewline\"}";
        ConfigObject obj = JSONCodec.loadConfig(json);

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Value\nNewline", obj.asSection().getString("key"));

        json = "{\"key\":\"Value\\\\Hello\"}";
        obj = JSONCodec.loadConfig(json);

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Value\\Hello", obj.asSection().getString("key"));
    }

    @Test
    public void testUnicode() {

        String json = "{\"key\":\"Unicode \\u0123\"}";
        ConfigObject obj = JSONCodec.loadConfig(json);

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Unicode \u0123", obj.asSection().getString("key"));


        json = "{\"key\":\"Unicode \\uuuu5432\"}";
        obj = JSONCodec.loadConfig(json);

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Unicode \u5432", obj.asSection().getString("key"));

        json = "{\"key\":\"Unicode \\u0123\\u5432\"}";
        obj = JSONCodec.loadConfig(json);

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Unicode \u0123\u5432", obj.asSection().getString("key"));


        try {
            ConfigObject sec = JSONCodec.fileCodec().loadFromFile(ConfigContext.INSTANCE, new File("Unicode.json"), StandardCharsets.UTF_8);
            Assertions.assertTrue(sec.isSection());

            String value = sec.asSection().getString("Test");
            Assertions.assertEquals(1, value.length());
            Assertions.assertEquals("\u30BF", value);

        } catch (IOException ex) {
            Assertions.fail();
        }
    }

    @Test
    public void testInvalid() {

        Assertions.assertThrows(DecodeException.class, () -> {
            JSONCodec.loadConfig("{\"key\":\"Invalid Value}");
        });


    }

}
