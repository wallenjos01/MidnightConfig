import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigPrimitive;

public class TestConfigPrimitive {

    @Test
    public void testBasics() {

        ConfigPrimitive str = new ConfigPrimitive("Hello");
        Assertions.assertTrue(str.isPrimitive());
        Assertions.assertTrue(str.isString());
        Assertions.assertEquals("Hello", str.getValue());
        Assertions.assertEquals(new ConfigPrimitive("Hello"), str); // Test equals() method

        ConfigPrimitive num = new ConfigPrimitive(42);
        Assertions.assertTrue(num.isPrimitive());
        Assertions.assertTrue(num.isNumber());
        Assertions.assertEquals(42, num.getValue());
        Assertions.assertEquals(42, num.asNumber());
        Assertions.assertEquals(42L, num.asLong());
        Assertions.assertEquals(42, num.asInt());
        Assertions.assertEquals((short) 42, num.asShort());
        Assertions.assertEquals((byte) 42, num.asByte());
        Assertions.assertEquals(42.0f, num.asFloat());
        Assertions.assertEquals(42.0, num.asDouble());
        Assertions.assertEquals(new ConfigPrimitive(42), num); // Test equals() method


        ConfigPrimitive bool = new ConfigPrimitive(true);
        Assertions.assertTrue(bool.isPrimitive());
        Assertions.assertTrue(bool.isBoolean());
        Assertions.assertEquals(true, bool.getValue());
        Assertions.assertEquals(new ConfigPrimitive(true), bool); // Test equals() method

    }

}
