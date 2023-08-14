import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestConfigContext {


    @Test
    public void testRead() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        ConfigPrimitive primStr = new ConfigPrimitive("String");
        String str = ctx.asString(primStr);

        Assertions.assertNotNull(str);
        Assertions.assertEquals("String", str);
        Assertions.assertNull(ctx.asNumber(primStr));

        ConfigPrimitive primNum = new ConfigPrimitive(42);
        Number num = ctx.asNumber(primNum);

        Assertions.assertNotNull(num);
        Assertions.assertEquals(42, num.intValue());
        Assertions.assertNull(ctx.asString(primNum));

        ConfigPrimitive primBool = new ConfigPrimitive(false);
        Boolean bool = ctx.asBoolean(primBool);

        Assertions.assertNotNull(bool);
        Assertions.assertEquals(false, bool);
        Assertions.assertNull(ctx.asNumber(primBool));

        ConfigList list = new ConfigList();
        list.add(37);
        list.add("Hello");

        Collection<ConfigObject> objList = ctx.asList(list);
        Assertions.assertNotNull(objList);
        Assertions.assertEquals(2, objList.size());

        List<ConfigObject> objArray = new ArrayList<>(objList);
        Assertions.assertEquals(37, ctx.asNumber(objArray.get(0)));
        Assertions.assertEquals("Hello", ctx.asString(objArray.get(1)));

        ConfigSection section = new ConfigSection().with("Key", "Value");
        Map<String, ConfigObject> objMap = ctx.asMap(section);
        Assertions.assertNotNull(objMap);
        Assertions.assertEquals(1, objMap.size());
        Assertions.assertTrue(objMap.containsKey("Key"));
        Assertions.assertTrue(objMap.get("Key").isString());
        Assertions.assertEquals("Value", objMap.get("Key").asString());
        Assertions.assertEquals("Value", ctx.asString(objMap.get("Key")));
        Assertions.assertEquals("Value", ctx.asString(ctx.get("Key", section)));

    }

    @Test
    public void testWrite() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        Assertions.assertEquals("String", ctx.toString("String").asString());
        Assertions.assertEquals(42, ctx.toNumber(42).asNumber());
        Assertions.assertEquals(true, ctx.toBoolean(true).asBoolean());

        ConfigObject list = ctx.toList(List.of(ctx.toString("Hello"), ctx.toString("Goodbye")));
        Assertions.assertTrue(list.isList());
        Assertions.assertEquals(2, list.asList().size());
        Assertions.assertEquals("Hello", list.asList().get(0).asString());
        Assertions.assertEquals("Goodbye", list.asList().get(1).asString());

        ConfigObject map = ctx.toMap(Map.of("Key", ctx.toString("Value")));
        Assertions.assertTrue(map.isSection());
        Assertions.assertEquals(1, map.asSection().size());
        Assertions.assertEquals("Value", map.asSection().getString("Key"));

    }

    @Test
    public void testFill() {

        ConfigContext ctx = ConfigContext.INSTANCE;

        ConfigSection section = new ConfigSection().with("Key1", "Value1");
        ConfigObject obj = ctx.mergeMap(section, ctx.toMap(Map.of("Key2", ctx.toString("Value2"))));

        // Fill
        Assertions.assertSame(section, obj);
        Assertions.assertTrue(section.isSection());
        Assertions.assertEquals(2, section.size());
        Assertions.assertEquals("Value1", section.getString("Key1"));
        Assertions.assertEquals("Value2", section.getString("Key2"));

        // Overwriting
        obj = ctx.mergeMapOverwrite(section, ctx.toMap(Map.of("Key1", ctx.toString("Value3"))));
        Assertions.assertSame(section, obj);
        Assertions.assertTrue(section.isSection());
        Assertions.assertEquals(2, section.size());
        Assertions.assertEquals("Value3", section.getString("Key1"));
        Assertions.assertEquals("Value2", section.getString("Key2"));

        // Non-destructive fill
        obj = ctx.mergeMap(section, ctx.toMap(Map.of("Key1", ctx.toString("Value4"))));
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

}
