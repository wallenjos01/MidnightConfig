import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.NumberSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestConfigSection {

    @Test
    public void testBasics() {

        // Initialization
        ConfigSection test = new ConfigSection();
        Assertions.assertEquals(0, test.size());

        // Adding a raw entry
        Assertions.assertNull(test.set("Test", new ConfigPrimitive(12)));
        Assertions.assertEquals(1, test.size());
        Assertions.assertTrue(test.get("Test").isPrimitive());
        Assertions.assertTrue(test.get("Test").asPrimitive().isNumber());
        Assertions.assertEquals(12, test.get("Test").asPrimitive().asInt());

        // Adding a String entry
        Assertions.assertNull(test.set("Hello", "World"));
        Assertions.assertEquals(2, test.size());
        Assertions.assertEquals("World", test.getString("Hello"));

        // Adding an Integer entry
        Assertions.assertNull(test.set("Number", 42));
        Assertions.assertEquals(3, test.size());
        Assertions.assertEquals(42, test.getInt("Number"));
        Assertions.assertEquals("World", test.getString("Hello"));

        // Removing an entry
        ConfigObject removed = test.remove("Hello");
        Assertions.assertEquals(2, test.size());
        Assertions.assertEquals(42, test.getInt("Number"));
        Assertions.assertEquals("World", removed.asPrimitive().asString());
        Assertions.assertNull(test.get("Hello"));

        // Overwriting an entry
        ConfigObject overwritten = test.set("Number", true);
        Assertions.assertEquals(2, test.size());
        Assertions.assertTrue(overwritten.isPrimitive());
        Assertions.assertTrue(overwritten.asPrimitive().isNumber());
        Assertions.assertEquals(42, overwritten.asNumber());

        Assertions.assertTrue(test.get("Number").isPrimitive());
        Assertions.assertTrue(test.get("Number").isBoolean());
        Assertions.assertEquals(true, test.get("Number").asBoolean());

    }

    private static class TestSerializable {

        final String strValue;
        final int numValue;

        public TestSerializable(String strValue, int numValue) {
            this.strValue = strValue;
            this.numValue = numValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public int getNumValue() {
            return numValue;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null || obj.getClass() != getClass()) return false;

            return ((TestSerializable) obj).strValue.equals(strValue)  && ((TestSerializable) obj).numValue == numValue;
        }
    }

    @Test
    public void testBySerializer() {

        Serializer<TestSerializable> serializer = ObjectSerializer.create(
                Serializer.STRING.entry("strValue", TestSerializable::getStrValue),
                Serializer.INT.entry("intValue", TestSerializable::getNumValue),
                TestSerializable::new
        );


        ConfigSection section = new ConfigSection();
        TestSerializable test = new TestSerializable("Hello", 42);

        Assertions.assertNull(section.set("Test", test, serializer));
        Assertions.assertEquals(1, section.size());

        TestSerializable deserialized = section.get("Test", serializer);

        Assertions.assertEquals(test, deserialized);

    }

    @Test
    public void testCopying() {

        ConfigSection sec1 = new ConfigSection()
                .with("key1", "value1")
                .with("key2", 2)
                .with("key3", true)
                .with("key4", new ConfigList())
                .with("key5", new ConfigSection());

        ConfigSection copy = sec1.copy();

        Assertions.assertEquals(sec1, copy);

        copy.set("Hello", "World");
        Assertions.assertNotEquals(sec1, copy);
        Assertions.assertEquals(5, sec1.size());
        Assertions.assertEquals(6, copy.size());

    }

    @Test
    public void testListFiltering() {

        ConfigSection section = new ConfigSection()
                .with("list", new ConfigList()
                        .append(1)
                        .append(4)
                        .append(-1));

        Serializer<Integer> ser = NumberSerializer.forInt(0, 100);
        List<Integer> is = section.getListFiltered("list", ser);

        Assertions.assertEquals(2, is.size());
        Assertions.assertEquals(1, is.get(0));
        Assertions.assertEquals(4, is.get(1));

        AtomicInteger errors = new AtomicInteger();
        is = section.getListFiltered("list", ser, error -> errors.getAndIncrement());
        Assertions.assertEquals(2, is.size());
        Assertions.assertEquals(1, is.get(0));
        Assertions.assertEquals(4, is.get(1));
        Assertions.assertEquals(1, errors.get());

    }

}
