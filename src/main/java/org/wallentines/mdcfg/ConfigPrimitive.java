package org.wallentines.mdcfg;

import java.math.BigInteger;
import java.util.Objects;

@SuppressWarnings("unused")
public class ConfigPrimitive implements ConfigObject {

    private final Object value;

    public ConfigPrimitive(Number value) {
        this((Object) value);
    }

    public ConfigPrimitive(Boolean value) {
        this((Object) value);
    }

    public ConfigPrimitive(String value) {
        this((Object) value);
    }

    private ConfigPrimitive(Object value) {
        if(value == null) throw new IllegalArgumentException("Cannot construct Config Primitive from null input!");
        this.value = value;
    }

    public byte asByte() {
        return asNumber().byteValue();
    }

    public char asChar() {
        return (char) asNumber().byteValue();
    }

    public short asShort() {
        return asNumber().shortValue();
    }

    public int asInt() {
        return asNumber().intValue();
    }

    public long asLong() {
        return asNumber().longValue();
    }

    public float asFloat() {
        return asNumber().floatValue();
    }

    public double asDouble() {
        return asNumber().doubleValue();
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean isNumber() {
        return value instanceof Number;
    }

    @Override
    public boolean isString() {
        return value instanceof String;
    }

    @Override
    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @Override
    public Number asNumber() {
        return (Number) value;
    }

    @Override
    public String asString() {
        return (String) value;
    }

    @Override
    public Boolean asBoolean() {
        return (Boolean) value;
    }

    @Override
    public ConfigPrimitive asPrimitive() {
        return this;
    }

    @Override
    public ConfigList asList() {
        throw new IllegalStateException("Cannot convert a primitive to a list!");
    }

    @Override
    public ConfigSection asSection() {
        throw new IllegalStateException("Cannot convert a primitive to a ConfigSection!");
    }

    @Override
    public ConfigPrimitive copy() {
        return this;
    }

    @Override
    public String toString() {
        return "ConfigPrimitive{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigPrimitive that = (ConfigPrimitive) o;
        if(isNumber()) {
            if(!that.isNumber()) return false;

            Number self = asNumber();
            Number other = that.asNumber();

            if(isInteger(self) && isInteger(other)) {
                return self.longValue() == other.longValue();
            }

            double selfD = self.doubleValue();
            double otherD = other.doubleValue();
            return asNumber().doubleValue() == that.asNumber().doubleValue() || Double.isNaN(selfD) && Double.isNaN(otherD);
        }

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public static boolean isInteger(Number number) {
        return number instanceof Integer || number instanceof Long || number instanceof Short || number instanceof Byte || number instanceof BigInteger;
    }

}
