import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.serializer.GsonContext;

import java.util.*;

public class TestGsonContext {

    @Test
    public void testRead() {

        GsonContext ctx = GsonContext.INSTANCE;

        JsonPrimitive primStr = new JsonPrimitive("String");
        String str = ctx.asString(primStr).getOrThrow();

        Assertions.assertNotNull(str);
        Assertions.assertEquals("String", str);
        Assertions.assertFalse(ctx.asNumber(primStr).isComplete());

        JsonPrimitive primNum = new JsonPrimitive(42);
        Number num = ctx.asNumber(primNum).getOrThrow();

        Assertions.assertNotNull(num);
        Assertions.assertEquals(42, num.intValue());
        Assertions.assertFalse(ctx.asString(primNum).isComplete());

        JsonPrimitive primBool = new JsonPrimitive(false);
        Boolean bool = ctx.asBoolean(primBool).getOrThrow();

        Assertions.assertNotNull(bool);
        Assertions.assertEquals(false, bool);
        Assertions.assertFalse(ctx.asNumber(primStr).isComplete());

        JsonArray list = new JsonArray();
        list.add(37);
        list.add("Hello");

        Collection<JsonElement> objList = ctx.asList(list).getOrThrow();
        Assertions.assertNotNull(objList);
        Assertions.assertEquals(2, objList.size());

        List<JsonElement> objArray = new ArrayList<>(objList);
        Assertions.assertEquals(37, ctx.asNumber(objArray.get(0)).getOrThrow());
        Assertions.assertEquals("Hello", ctx.asString(objArray.get(1)).getOrThrow());

        JsonObject object = new JsonObject();
        object.add("Key", new JsonPrimitive("Value"));

        Map<String, JsonElement> objMap = ctx.asMap(object).getOrThrow();
        Assertions.assertEquals(1, objMap.size());
        Assertions.assertTrue(objMap.containsKey("Key"));
        Assertions.assertTrue(objMap.get("Key").isJsonPrimitive());
        Assertions.assertTrue(objMap.get("Key").getAsJsonPrimitive().isString());
        Assertions.assertEquals("Value", objMap.get("Key").getAsString());
        Assertions.assertEquals("Value", ctx.asString(objMap.get("Key")).getOrThrow());
        Assertions.assertEquals("Value", ctx.asString(ctx.get("Key", object)).getOrThrow());

    }

    @Test
    public void testWrite() {

        GsonContext ctx = GsonContext.INSTANCE;

        Assertions.assertEquals("String", ctx.toString("String").getAsString());
        Assertions.assertEquals(42, ctx.toNumber(42).getAsNumber());
        Assertions.assertEquals(true, ctx.toBoolean(true).getAsBoolean());

        JsonElement list = ctx.toList(Arrays.asList(ctx.toString("Hello"), ctx.toString("Goodbye")));
        Assertions.assertTrue(list.isJsonArray());
        Assertions.assertEquals(2, list.getAsJsonArray().size());
        Assertions.assertEquals("Hello", list.getAsJsonArray().get(0).getAsString());
        Assertions.assertEquals("Goodbye", list.getAsJsonArray().get(1).getAsString());

        JsonElement map = ctx.toMap(makeMap("Key", ctx.toString("Value")));
        Assertions.assertTrue(map.isJsonObject());
        Assertions.assertEquals(1, map.getAsJsonObject().size());
        Assertions.assertEquals("Value", map.getAsJsonObject().get("Key").getAsString());

    }

    @Test
    public void testFill() {

        GsonContext ctx = GsonContext.INSTANCE;

        JsonObject object = new JsonObject();
        object.add("Key1", new JsonPrimitive("Value1"));
        JsonElement obj = ctx.mergeMap(object, ctx.toMap(makeMap("Key2", ctx.toString("Value2"))));

        // Fill
        Assertions.assertSame(object, obj);
        Assertions.assertTrue(object.isJsonObject());
        Assertions.assertEquals(2, object.size());
        Assertions.assertEquals("Value1", object.get("Key1").getAsString());
        Assertions.assertEquals("Value2", object.get("Key2").getAsString());

        // Overwriting
        obj = ctx.mergeMapOverwrite(object, ctx.toMap(makeMap("Key1", ctx.toString("Value3"))));
        Assertions.assertSame(object, obj);
        Assertions.assertTrue(object.isJsonObject());
        Assertions.assertEquals(2, object.size());
        Assertions.assertEquals("Value3", object.get("Key1").getAsString());
        Assertions.assertEquals("Value2", object.get("Key2").getAsString());

        // Non-destructive fill
        obj = ctx.mergeMap(object, ctx.toMap(makeMap("Key1", ctx.toString("Value4"))));
        Assertions.assertSame(object, obj);
        Assertions.assertTrue(object.isJsonObject());
        Assertions.assertEquals(2, object.size());
        Assertions.assertEquals("Value3", object.get("Key1").getAsString());
        Assertions.assertEquals("Value2", object.get("Key2").getAsString());
    }

    @Test
    public void testMerge() {

        GsonContext ctx = GsonContext.INSTANCE;

        JsonObject sec = new JsonObject();
        sec.add("Key1", new JsonPrimitive("Value1"));

        JsonObject sec2 = new JsonObject();
        sec2.add("Key1", new JsonPrimitive("Other"));
        sec2.add("Key2", new JsonPrimitive("Value2"));

        JsonElement merged = ctx.merge(sec, sec2);

        Assertions.assertTrue(merged.isJsonObject());
        Assertions.assertEquals(2, merged.getAsJsonObject().size());
        Assertions.assertEquals("Value1", merged.getAsJsonObject().get("Key1").getAsString());
        Assertions.assertEquals("Value2", merged.getAsJsonObject().get("Key2").getAsString());

    }

    private <K,V> Map<K,V> makeMap(K key, V value) {

        HashMap<K, V> out = new HashMap<>();
        out.put(key, value);

        return out;
    }

    @Test
    public void testCopy() {

        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        arr.add(new JsonPrimitive("Hello"));

        JsonObject child = new JsonObject();
        child.add("key", new JsonPrimitive("value"));

        JsonObject object = new JsonObject();
        object.add("string", new JsonPrimitive("str"));
        object.add("number", new JsonPrimitive(12));
        object.add("bool", new JsonPrimitive(true));
        object.add("list", arr);
        object.add("object", child);

        JsonElement cloned = GsonContext.INSTANCE.copy(object);

        Assertions.assertTrue(cloned.isJsonObject());
        Assertions.assertNotSame(cloned, object);

        Assertions.assertEquals(5, cloned.getAsJsonObject().size());
        Assertions.assertEquals("str", cloned.getAsJsonObject().get("string").getAsString());
        Assertions.assertEquals(12, cloned.getAsJsonObject().get("number").getAsNumber());
        Assertions.assertTrue(cloned.getAsJsonObject().get("bool").getAsBoolean());

        Assertions.assertEquals(2, cloned.getAsJsonObject().getAsJsonArray("list").size());
        Assertions.assertEquals(object.getAsJsonArray("list"), cloned.getAsJsonObject().getAsJsonArray("list"));
        Assertions.assertNotSame(object.getAsJsonArray("list"), cloned.getAsJsonObject().getAsJsonArray("list"));

        Assertions.assertEquals(1, cloned.getAsJsonObject().getAsJsonObject("object").size());
        Assertions.assertEquals(object.getAsJsonObject("object"), cloned.getAsJsonObject().getAsJsonObject("object"));
        Assertions.assertNotSame(object.getAsJsonObject("object"), cloned.getAsJsonObject().getAsJsonObject("object"));

    }

}
