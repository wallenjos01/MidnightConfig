import com.google.gson.JsonElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.GsonCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.GsonContext;

public class TestGson {

    @Test
    public void testEncode() {

        ConfigSection test = new ConfigSection();
        test.set("String", "Hello, World");
        test.set("Number", 42);
        test.set("List", ConfigList.of("String", 69, 112.4));
        test.set("Section", new ConfigSection().with("Hello", "World"));

        // Non-indented
        GsonCodec codec = GsonCodec.minified();
        Assertions.assertEquals("{\"String\":\"Hello, World\",\"Number\":42,\"List\":[\"String\",69,112.4],\"Section\":{\"Hello\":\"World\"}}", codec.encodeToString(ConfigContext.INSTANCE, test));

        // Indented
        codec = GsonCodec.readable();
        Assertions.assertEquals("{\n  \"String\": \"Hello, World\",\n  \"Number\": 42,\n  \"List\": [\n    \"String\",\n    69,\n    112.4\n  ],\n  \"Section\": {\n    \"Hello\": \"World\"\n  }\n}", codec.encodeToString(ConfigContext.INSTANCE, test));

    }

    @Test
    public void testDecode() {

        GsonCodec codec = GsonCodec.minified();

        String encodedPrimitive = "\"Hello\"";
        ConfigObject primitive = codec.decode(ConfigContext.INSTANCE, encodedPrimitive);

        Assertions.assertTrue(primitive.isPrimitive());
        Assertions.assertTrue(primitive.asPrimitive().isString());
        Assertions.assertEquals("Hello", primitive.asPrimitive().asString());

        String encodedNumber = "1000";
        ConfigObject number = codec.decode(ConfigContext.INSTANCE, encodedNumber);

        Assertions.assertTrue(number.isPrimitive());
        Assertions.assertTrue(number.asPrimitive().isNumber());
        Assertions.assertEquals(1000, number.asPrimitive().asNumber().intValue());


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
    public void testQuotes() {

        // Quoted Values
        ConfigSection sec = new ConfigSection().with("Key", "\"Quoted String\"");
        String encoded = GsonCodec.minified().encodeToString(ConfigContext.INSTANCE, sec);
        Assertions.assertEquals("{\"Key\":\"\\\"Quoted String\\\"\"}", encoded);

        JsonElement obj = GsonCodec.minified().decode(GsonContext.INSTANCE, encoded);
        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("\"Quoted String\"", obj.getAsJsonObject().get("Key").getAsString());

        // Quoted Keys
        sec = new ConfigSection().with("\"Quoted Key\"", "Value");
        encoded = GsonCodec.minified().encodeToString(ConfigContext.INSTANCE, sec);
        Assertions.assertEquals("{\"\\\"Quoted Key\\\"\":\"Value\"}", encoded);

        obj = GsonCodec.minified().decode(GsonContext.INSTANCE, encoded);
        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("Value", obj.getAsJsonObject().get("\"Quoted Key\"").getAsString());

    }

    @Test
    public void testEscape() {

        String json = "{\"key\":\"Value\\nNewline\"}";
        JsonElement obj = GsonCodec.minified().decode(GsonContext.INSTANCE, json);

        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("Value\nNewline", obj.getAsJsonObject().get("key").getAsString());

        json = "{\"key\":\"Value\\\\Hello\"}";
        obj = GsonCodec.minified().decode(GsonContext.INSTANCE, json);

        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("Value\\Hello", obj.getAsJsonObject().get("key").getAsString());
    }

    @Test
    public void testUnicode() {

        String json = "{\"key\":\"Unicode \\u0123\"}";
        JsonElement obj = GsonCodec.minified().decode(GsonContext.INSTANCE, json);

        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("Unicode \u0123", obj.getAsJsonObject().get("key").getAsString());


        json = "{\"key\":\"Unicode \\u5432\"}";
        obj = GsonCodec.minified().decode(GsonContext.INSTANCE, json);

        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("Unicode \u5432", obj.getAsJsonObject().get("key").getAsString());

        json = "{\"key\":\"Unicode \\u0123\\u5432\"}";
        obj = GsonCodec.minified().decode(GsonContext.INSTANCE, json);

        Assertions.assertTrue(obj.isJsonObject());
        Assertions.assertEquals("Unicode \u0123\u5432", obj.getAsJsonObject().get("key").getAsString());

    }

    @Test
    public void testInvalid() {

        String invalid = "{\"key\":\"Invalid Value}";

        boolean caught = false;
        try {
            GsonCodec.minified().decode(GsonContext.INSTANCE, invalid);
        } catch (DecodeException ex) {
            caught = true;
        }

        Assertions.assertTrue(caught);

    }
    
}
