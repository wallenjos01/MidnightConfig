import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.util.*;

public class TestConfigContext {


    @Test
    public void testRead() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        ConfigPrimitive primStr = new ConfigPrimitive("String");
        String str = ctx.asString(primStr).getOrThrow();

        Assertions.assertNotNull(str);
        Assertions.assertEquals("String", str);
        Assertions.assertFalse(ctx.asNumber(primStr).isComplete());

        ConfigPrimitive primNum = new ConfigPrimitive(42);
        Number num = ctx.asNumber(primNum).getOrThrow();

        Assertions.assertNotNull(num);
        Assertions.assertEquals(42, num.intValue());
        Assertions.assertFalse(ctx.asString(primNum).isComplete());

        ConfigPrimitive primBool = new ConfigPrimitive(false);
        Boolean bool = ctx.asBoolean(primBool).getOrThrow();

        Assertions.assertNotNull(bool);
        Assertions.assertEquals(false, bool);
        Assertions.assertFalse(ctx.asNumber(primStr).isComplete());

        ConfigList list = new ConfigList();
        list.add(37);
        list.add("Hello");

        Collection<ConfigObject> objList = ctx.asList(list).getOrThrow();
        Assertions.assertNotNull(objList);
        Assertions.assertEquals(2, objList.size());

        List<ConfigObject> objArray = new ArrayList<>(objList);
        Assertions.assertEquals(37, ctx.asNumber(objArray.get(0)).getOrThrow());
        Assertions.assertEquals("Hello", ctx.asString(objArray.get(1)).getOrThrow());

        ConfigSection section = new ConfigSection().with("Key", "Value");
        Map<String, ConfigObject> objMap = ctx.asMap(section).getOrThrow();
        Assertions.assertEquals(1, objMap.size());
        Assertions.assertTrue(objMap.containsKey("Key"));
        Assertions.assertTrue(objMap.get("Key").isString());
        Assertions.assertEquals("Value", objMap.get("Key").asString());
        Assertions.assertEquals("Value", ctx.asString(objMap.get("Key")).getOrThrow());
        Assertions.assertEquals("Value", ctx.asString(ctx.get("Key", section)).getOrThrow());

    }

    @Test
    public void testWrite() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        Assertions.assertEquals("String", ctx.toString("String").asString());
        Assertions.assertEquals(42, ctx.toNumber(42).asNumber());
        Assertions.assertEquals(true, ctx.toBoolean(true).asBoolean());

        ConfigObject list = ctx.toList(Arrays.asList(ctx.toString("Hello"), ctx.toString("Goodbye")));
        Assertions.assertTrue(list.isList());
        Assertions.assertEquals(2, list.asList().size());
        Assertions.assertEquals("Hello", list.asList().get(0).asString());
        Assertions.assertEquals("Goodbye", list.asList().get(1).asString());

        ConfigObject map = ctx.toMap(makeMap("Key", ctx.toString("Value")));
        Assertions.assertTrue(map.isSection());
        Assertions.assertEquals(1, map.asSection().size());
        Assertions.assertEquals("Value", map.asSection().getString("Key"));

    }

    @Test
    public void testFill() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        ConfigSection section = new ConfigSection().with("Key1", "Value1");
        ConfigObject obj = ctx.mergeMap(section, ctx.toMap(makeMap("Key2", ctx.toString("Value2"))));

        // Fill
        Assertions.assertSame(section, obj);
        Assertions.assertTrue(section.isSection());
        Assertions.assertEquals(2, section.size());
        Assertions.assertEquals("Value1", section.getString("Key1"));
        Assertions.assertEquals("Value2", section.getString("Key2"));

        // Overwriting
        obj = ctx.mergeMapOverwrite(section, ctx.toMap(makeMap("Key1", ctx.toString("Value3"))));
        Assertions.assertSame(section, obj);
        Assertions.assertTrue(section.isSection());
        Assertions.assertEquals(2, section.size());
        Assertions.assertEquals("Value3", section.getString("Key1"));
        Assertions.assertEquals("Value2", section.getString("Key2"));

        // Non-destructive fill
        obj = ctx.mergeMap(section, ctx.toMap(makeMap("Key1", ctx.toString("Value4"))));
        Assertions.assertSame(section, obj);
        Assertions.assertTrue(section.isSection());
        Assertions.assertEquals(2, section.size());
        Assertions.assertEquals("Value3", section.getString("Key1"));
        Assertions.assertEquals("Value2", section.getString("Key2"));
    }

    @Test
    public void testMerge() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        ConfigSection sec = new ConfigSection().with("Key1", "Value1");
        ConfigSection sec2 = new ConfigSection().with("Key1", "Other").with("Key2", "Value2");

        ConfigObject merged = ctx.merge(sec, sec2);

        Assertions.assertTrue(merged.isSection());
        Assertions.assertEquals(2, merged.asSection().size());
        Assertions.assertEquals("Value1", merged.asSection().getString("Key1"));
        Assertions.assertEquals("Value2", merged.asSection().getString("Key2"));

    }

    private <K,V> Map<K,V> makeMap(K key, V value) {

        HashMap<K, V> out = new HashMap<>();
        out.put(key, value);

        return out;
    }

    @Test
    public void testCopy() {

        ConfigSection section = new ConfigSection()
                .with("string", "str")
                .with("number", 12)
                .with("bool", true)
                .with("list", new ConfigList().append(1).append("Hello"))
                .with("section", new ConfigSection()
                        .with("key", "value"));

        ConfigObject cloned = ConfigContext.INSTANCE.copy(section);

        Assertions.assertTrue(cloned.isSection());
        Assertions.assertNotSame(cloned, section);

        Assertions.assertEquals(5, cloned.asSection().size());
        Assertions.assertEquals("str", cloned.asSection().getString("string"));
        Assertions.assertEquals(12, cloned.asSection().getInt("number"));
        Assertions.assertTrue(cloned.asSection().getBoolean("bool"));

        Assertions.assertEquals(2, cloned.asSection().getList("list").size());
        Assertions.assertEquals(section.getList("list"), cloned.asSection().getList("list"));
        Assertions.assertNotSame(section.getList("list"), cloned.asSection().getList("list"));

        Assertions.assertEquals(1, cloned.asSection().getSection("section").size());
        Assertions.assertEquals(section.getSection("section"), cloned.asSection().getSection("section"));
        Assertions.assertNotSame(section.getSection("section"), cloned.asSection().getSection("section"));

    }

}
