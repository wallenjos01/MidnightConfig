package org.wallentines.mdcfg;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.math.BigInteger;
import java.util.Objects;

@SuppressWarnings("unused")
public class ConfigPrimitive extends ConfigObject {

    private final Object value;

    /**
     * Creates a ConfigPrimitive with the given Number value
     * @param value The Number value
     * @throws IllegalArgumentException If the value is null
     */
    public ConfigPrimitive(Number value) {
        this((Object) value);
    }

    /**
     * Creates a ConfigPrimitive with the given Boolean value
     * @param value The Boolean value
     * @throws IllegalArgumentException If the value is null
     */
    public ConfigPrimitive(Boolean value) {
        this((Object) value);
    }

    /**
     * Creates a ConfigPrimitive with the given String value
     * @param value The String value
     * @throws IllegalArgumentException If the value is null
     */
    public ConfigPrimitive(String value) {
        this((Object) value);
    }

    private ConfigPrimitive(Object value) {
        if(value == null) throw new IllegalArgumentException("Cannot construct Config Primitive from null input!");
        this.value = value;
    }


    /**
     * Retrieves the value of this primitive as a byte
     * @return The byte value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public byte asByte() {
        return asNumber().byteValue();
    }

    /**
     * Retrieves the value of this primitive as a char
     * @return The char value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public char asChar() {
        return (char) asNumber().intValue();
    }

    /**
     * Retrieves the value of this primitive as a short
     * @return The short value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public short asShort() {
        return asNumber().shortValue();
    }

    /**
     * Retrieves the value of this primitive as an int
     * @return The int value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public int asInt() {
        return asNumber().intValue();
    }

    /**
     * Retrieves the value of this primitive as a long
     * @return The long value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public long asLong() {
        return asNumber().longValue();
    }

    /**
     * Retrieves the value of this primitive as a float
     * @return The float value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public float asFloat() {
        return asNumber().floatValue();
    }

    /**
     * Retrieves the value of this primitive as a double
     * @return The double value of this primitive
     * @throws IllegalStateException If the value is not a Number
     */
    public double asDouble() {
        return asNumber().doubleValue();
    }

    /**
     * Retrieves the value of this primitive
     * @return The value of this primitive. Will always be either a String, Number, or Boolean
     */
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
    public String asString() {
        if(!isString()) throw new IllegalStateException("Cannot convert " + value + " to a String!");
        return (String) value;
    }

    @Override
    public Number asNumber() {
        if(!isNumber()) throw new IllegalStateException("Cannot convert " + value + " to a Number!");
        return (Number) value;
    }

    @Override
    public Boolean asBoolean() {
        if(!isBoolean()) throw new IllegalStateException("Cannot convert " + value + " to a Boolean!");
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
        if(isNumber()) {
            return new ConfigPrimitive(SerializeContext.copyNumber(asNumber()));
        }
        return new ConfigPrimitive(value);
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

    /**
     * Determines if a Number object is a type of integer (long, int, short, etc.)
     * @param number The number to check
     * @return Whether the number is a type of integer
     */
    public static boolean isInteger(Number number) {
        return number instanceof Integer || number instanceof Long || number instanceof Short || number instanceof Byte || number instanceof BigInteger;
    }

}
