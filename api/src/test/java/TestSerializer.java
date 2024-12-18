import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.*;

import java.util.*;

public class TestSerializer {


    @Test
    public void testPrimitives() {

        String str = "string";

        byte int8 = (byte) 12;
        short int16 = (short) 5163;
        int int32 = 19203840;
        long int64 = 92130912939384L;

        float float32 = 33.14245f;
        double float64 = 9090143.975489379854;

        boolean bool = true;

        ConfigContext ctx = new ConfigContext();

        Assertions.assertEquals(str, Serializer.STRING.deserialize(ctx, Serializer.STRING.serialize(ctx, str).getOrThrow()).getOrThrow());

        Assertions.assertEquals(int8, Serializer.BYTE.deserialize(ctx, Serializer.BYTE.serialize(ctx, int8).getOrThrow()).getOrThrow());
        Assertions.assertEquals(int16, Serializer.SHORT.deserialize(ctx, Serializer.SHORT.serialize(ctx, int16).getOrThrow()).getOrThrow());
        Assertions.assertEquals(int32, Serializer.INT.deserialize(ctx, Serializer.INT.serialize(ctx, int32).getOrThrow()).getOrThrow());
        Assertions.assertEquals(int64, Serializer.LONG.deserialize(ctx, Serializer.LONG.serialize(ctx, int64).getOrThrow()).getOrThrow());
        Assertions.assertEquals(float32, Serializer.FLOAT.deserialize(ctx, Serializer.FLOAT.serialize(ctx, float32).getOrThrow()).getOrThrow());
        Assertions.assertEquals(float64, Serializer.DOUBLE.deserialize(ctx, Serializer.DOUBLE.serialize(ctx, float64).getOrThrow()).getOrThrow());

        Assertions.assertEquals(bool, Serializer.BOOLEAN.deserialize(ctx, Serializer.BOOLEAN.serialize(ctx, bool).getOrThrow()).getOrThrow());

        Assertions.assertEquals(0.0f, Serializer.FLOAT.deserialize(ctx, new ConfigPrimitive(0.0f)).getOrThrow());
    }

    static class TestSerializable {
        String value;
        public TestSerializable(String value) {
            this.value = value;
        }
    }

    @Test
    public void testSingle() {

        Serializer<TestSerializable> serializer = InlineSerializer.of(ts -> ts.value, TestSerializable::new);

        ConfigPrimitive input = new ConfigPrimitive("Hello");
        TestSerializable ser = serializer.deserialize(ConfigContext.INSTANCE, input).getOrThrow();
        Assertions.assertEquals(input.asString(), ser.value);

        ConfigObject out = serializer.serialize(ConfigContext.INSTANCE, ser).getOrThrow();
        Assertions.assertEquals(input, out);
    }


    static class TestSerializableMulti {

        String strValue;
        Integer intValue;
        Boolean boolValue;

        public TestSerializableMulti(String strValue, Integer intValue, Boolean boolValue) {
            this.strValue = strValue;
            this.intValue = intValue;
            this.boolValue = boolValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public Boolean getBoolValue() {
            return boolValue;
        }

        static final Serializer<TestSerializableMulti> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("string", TestSerializableMulti::getStrValue),
                Serializer.INT.entry("int", TestSerializableMulti::getIntValue),
                Serializer.BOOLEAN.entry("bool", TestSerializableMulti::getBoolValue),
                TestSerializableMulti::new
        );

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestSerializableMulti that = (TestSerializableMulti) o;
            return Objects.equals(strValue, that.strValue) && Objects.equals(intValue, that.intValue) && Objects.equals(boolValue, that.boolValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strValue, intValue, boolValue);
        }
    }

    @Test
    public void testMulti() {

        TestSerializableMulti multi = new TestSerializableMulti("Hello", 42, true);

        ConfigContext ctx = new ConfigContext();
        ConfigObject obj = TestSerializableMulti.SERIALIZER.serialize(ctx, multi).getOrThrow();

        Assertions.assertTrue(obj.isSection());
        Assertions.assertEquals("Hello", obj.asSection().getString("string"));
        Assertions.assertEquals(42, obj.asSection().getInt("int"));
        Assertions.assertTrue(obj.asSection().getBoolean("bool"));

        TestSerializableMulti out = TestSerializableMulti.SERIALIZER.deserialize(ctx, obj).getOrThrow();

        Assertions.assertEquals(multi.strValue, out.strValue);
        Assertions.assertEquals(multi.intValue, out.intValue);
        Assertions.assertEquals(multi.boolValue, out.boolValue);

    }

    static class TestSerializableComplex {

        String strValue;
        Integer intValue;
        Boolean boolValue;
        List<String> listValue = new ArrayList<>();
        Map<Integer, String> mapValue = new HashMap<>();
        TestSerializableMulti classValue;

        public TestSerializableComplex(String strValue, Integer intValue, Boolean boolValue, Collection<String> listValue, Map<Integer, String> mapValue, TestSerializableMulti classValue) {
            this.strValue = strValue;
            this.intValue = intValue;
            this.boolValue = boolValue;
            this.listValue.addAll(listValue);
            this.mapValue.putAll(mapValue);
            this.classValue = classValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public Boolean getBoolValue() {
            return boolValue;
        }

        public Collection<String> getListValue() {
            return listValue;
        }

        public Map<Integer, String> getMapValue() {
            return mapValue;
        }

        public TestSerializableMulti getClassValue() {
            return classValue;
        }
    }

    @Test
    public void testComplex() {

        HashMap<Integer, String> map = new HashMap<>();
        map.put(12, "Yes");
        map.put(33, "No");


        TestSerializableComplex multi = new TestSerializableComplex(
                "Hello",
                42,
                true,
                Arrays.asList("Hello", "World"),
                map,
                new TestSerializableMulti("str", 11, false));

        Serializer<TestSerializableComplex> serializer = ObjectSerializer.create(
                Serializer.STRING.entry("string", TestSerializableComplex::getStrValue),
                Serializer.INT.entry("int", TestSerializableComplex::getIntValue),
                Serializer.BOOLEAN.entry("bool", TestSerializableComplex::getBoolValue),
                Serializer.STRING.listOf().entry("list", TestSerializableComplex::getListValue),
                Serializer.STRING.mapOf(InlineSerializer.of(Object::toString, Integer::parseInt)).entry("map", TestSerializableComplex::getMapValue),
                TestSerializableMulti.SERIALIZER.entry("class", TestSerializableComplex::getClassValue),
                TestSerializableComplex::new
        );

        ConfigContext ctx = new ConfigContext();
        ConfigObject obj = serializer.serialize(ctx, multi).getOrThrow();

        Assertions.assertTrue(obj.isSection());

        Assertions.assertEquals("Hello", obj.asSection().getString("string"));
        Assertions.assertEquals(42, obj.asSection().getInt("int"));
        Assertions.assertTrue(obj.asSection().getBoolean("bool"));

        Assertions.assertTrue(obj.asSection().get("list").isList());
        Assertions.assertEquals(2, obj.asSection().getList("list").size());
        Assertions.assertEquals("Hello", obj.asSection().getList("list").get(0).asPrimitive().asString());
        Assertions.assertEquals("World", obj.asSection().getList("list").get(1).asPrimitive().asString());

        Assertions.assertTrue(obj.asSection().get("map").isSection());
        Assertions.assertEquals(2, obj.asSection().getSection("map").size());
        Assertions.assertEquals("Yes", obj.asSection().getSection("map").getString("12"));
        Assertions.assertEquals("No", obj.asSection().getSection("map").getString("33"));

        Assertions.assertTrue(obj.asSection().get("class").isSection());
        Assertions.assertEquals(3, obj.asSection().getSection("class").size());
        Assertions.assertEquals("str", obj.asSection().getSection("class").getString("string"));
        Assertions.assertEquals(11, obj.asSection().getSection("class").getInt("int"));
        Assertions.assertFalse(obj.asSection().getSection("class").getBoolean("bool"));

        TestSerializableComplex out = serializer.deserialize(ctx, obj).getOrThrow();

        Assertions.assertEquals(multi.strValue, out.strValue);
        Assertions.assertEquals(multi.intValue, out.intValue);
        Assertions.assertEquals(multi.boolValue, out.boolValue);

        Assertions.assertEquals(multi.listValue.size(), out.listValue.size());
        Assertions.assertEquals(multi.listValue.get(0), out.listValue.get(0));
        Assertions.assertEquals(multi.listValue.get(1), out.listValue.get(1));

        Assertions.assertEquals(multi.mapValue.size(), out.mapValue.size());
        Assertions.assertEquals(multi.mapValue.get(12), out.mapValue.get(12));
        Assertions.assertEquals(multi.mapValue.get(33), out.mapValue.get(33));

        Assertions.assertEquals(multi.classValue, out.classValue);

    }

    @Test
    public void testFailure() {

        ConfigContext ctx = new ConfigContext();
        ConfigSection section = new ConfigSection()
                .with("string", "str")
                .with("int", 22)
                .with("bool", "not a boolean");

        SerializeResult<TestSerializableMulti> result = TestSerializableMulti.SERIALIZER.deserialize(ctx, section);
        Assertions.assertFalse(result.isComplete());

        // Strings cannot be converted to Integers
        ConfigPrimitive dbl = new ConfigPrimitive("12.4465");
        SerializeResult<Integer> integer = Serializer.INT.deserialize(ctx, dbl);
        Assertions.assertFalse(integer.isComplete());

        // Floats/Doubles can be converted to Integers
        ConfigPrimitive numDbl = new ConfigPrimitive(12.4465);
        integer = Serializer.INT.deserialize(ctx, numDbl);
        Assertions.assertTrue(integer.isComplete());
    }

    @Test
    public void setBoundedNumbers() {

        // Bounded NumberSerializers
        int i1 = -1;
        int i2 = 101;
        int i3 = 50;
        NumberSerializer<Integer> serializer = NumberSerializer.forInt(0, 100);

        SerializeResult<ConfigObject> r1 = serializer.serialize(ConfigContext.INSTANCE, i1);
        SerializeResult<ConfigObject> r2 = serializer.serialize(ConfigContext.INSTANCE, i2);
        SerializeResult<ConfigObject> r3 = serializer.serialize(ConfigContext.INSTANCE, i3);

        Assertions.assertFalse(r1.isComplete());
        Assertions.assertFalse(r2.isComplete());
        Assertions.assertTrue(r3.isComplete());

        ConfigPrimitive c1 = new ConfigPrimitive(-1);
        ConfigPrimitive c2 = new ConfigPrimitive(101);
        ConfigPrimitive c3 = new ConfigPrimitive(50);

        SerializeResult<Integer> r4 = serializer.deserialize(ConfigContext.INSTANCE, c1);
        SerializeResult<Integer> r5 = serializer.deserialize(ConfigContext.INSTANCE, c2);
        SerializeResult<Integer> r6 = serializer.deserialize(ConfigContext.INSTANCE, c3);

        Assertions.assertFalse(r4.isComplete());
        Assertions.assertFalse(r5.isComplete());
        Assertions.assertTrue(r6.isComplete());

    }

    @Test
    public void testOr() {

        ConfigPrimitive rawInt = new ConfigPrimitive(1);
        ConfigPrimitive stringInt = new ConfigPrimitive("34");

        Serializer<Integer> stringSerializer = InlineSerializer.of(Object::toString, Integer::parseInt);
        Serializer<Integer> intSerializer = Serializer.INT;

        Assertions.assertEquals(34, stringSerializer.deserialize(ConfigContext.INSTANCE, stringInt).getOrThrow());
        Assertions.assertEquals(1, intSerializer.deserialize(ConfigContext.INSTANCE, rawInt).getOrThrow());

        Serializer<Integer> compoundSerializer = intSerializer.or(stringSerializer);

        Assertions.assertEquals(1, compoundSerializer.deserialize(ConfigContext.INSTANCE, rawInt).getOrThrow());
        Assertions.assertEquals(34, compoundSerializer.deserialize(ConfigContext.INSTANCE, stringInt).getOrThrow());

    }

    @Test
    public void testOptional() {

        Serializer<TestSerializableMulti> optionalSerializer = ObjectSerializer.create(
                Serializer.STRING.entry("string", TestSerializableMulti::getStrValue),
                Serializer.INT.entry("int", TestSerializableMulti::getIntValue),
                Serializer.BOOLEAN.entry("bool", TestSerializableMulti::getBoolValue).optional(),
                TestSerializableMulti::new
        );

        ConfigSection serialized = new ConfigSection().with("string", "value").with("int", 33);
        SerializeResult<TestSerializableMulti> result = optionalSerializer.deserialize(ConfigContext.INSTANCE, serialized);
        Assertions.assertTrue(result.isComplete());

        TestSerializableMulti multi = result.getOrThrow();

        Assertions.assertEquals("value", multi.getStrValue());
        Assertions.assertEquals(33, multi.getIntValue());
        Assertions.assertNull(multi.getBoolValue());

        SerializeResult<ConfigObject> sResult = optionalSerializer.serialize(ConfigContext.INSTANCE, multi);
        Assertions.assertTrue(sResult.isComplete());
        Assertions.assertEquals(2, sResult.getOrThrow().asSection().size());

    }

    @Test
    public void testDefault() {

        Serializer<TestSerializableMulti> defaultSerializer = ObjectSerializer.create(
                Serializer.STRING.entry("string", TestSerializableMulti::getStrValue),
                Serializer.INT.entry("int", TestSerializableMulti::getIntValue).orElse(12),
                Serializer.BOOLEAN.entry("bool", TestSerializableMulti::getBoolValue),
                TestSerializableMulti::new
        );

        ConfigSection serialized = new ConfigSection().with("string", "value").with("bool", true);
        SerializeResult<TestSerializableMulti> result = defaultSerializer.deserialize(ConfigContext.INSTANCE, serialized);
        Assertions.assertTrue(result.isComplete());

        TestSerializableMulti multi = result.getOrThrow();

        Assertions.assertEquals("value", multi.getStrValue());
        Assertions.assertEquals(12, multi.getIntValue());
        Assertions.assertEquals(true, multi.getBoolValue());

        SerializeResult<ConfigObject> sResult = defaultSerializer.serialize(ConfigContext.INSTANCE, multi);
        Assertions.assertTrue(sResult.isComplete());
        Assertions.assertEquals(3, sResult.getOrThrow().asSection().size());

    }

    static class StringWrapper {
        String str;

        public StringWrapper(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }

    @Test
    public void testMap() {

        Serializer<StringWrapper> serializer = Serializer.STRING.flatMap(StringWrapper::getStr, StringWrapper::new);

        ConfigPrimitive prim = new ConfigPrimitive("Hello");
        StringWrapper wrapper = serializer.deserialize(ConfigContext.INSTANCE, prim).getOrThrow();

        Assertions.assertEquals("Hello", wrapper.getStr());

        String serialized = serializer.serialize(ConfigContext.INSTANCE, wrapper).getOrThrow().asString();
        Assertions.assertEquals("Hello", serialized);
    }

    @Test
    public void testFieldOf() {

        Serializer<String> serializer = Serializer.STRING.fieldOf("key");

        ConfigSection sec = new ConfigSection()
                .with("key", "value");

        String value = serializer.deserialize(ConfigContext.INSTANCE, sec).getOrThrow();
        Assertions.assertEquals("value", value);

        ConfigSection serialized = serializer.serialize(ConfigContext.INSTANCE, value).getOrThrow().asSection();

        Assertions.assertEquals(sec, serialized);

    }

    @Test
    public void testAnd() {

        Serializer<Tuples.T2<String, Boolean>> serializer = Serializer.STRING.fieldOf("str").and(Serializer.BOOLEAN.fieldOf("bool"));

        ConfigSection sec = new ConfigSection()
                .with("str", "value")
                .with("bool", true);

        Tuples.T2<String, Boolean> value = serializer.deserialize(ConfigContext.INSTANCE, sec).getOrThrow();
        Assertions.assertEquals("value", value.p1);
        Assertions.assertTrue(value.p2);

        ConfigSection serialized = serializer.serialize(ConfigContext.INSTANCE, value).getOrThrow().asSection();

        Assertions.assertEquals(sec, serialized);

    }

}
