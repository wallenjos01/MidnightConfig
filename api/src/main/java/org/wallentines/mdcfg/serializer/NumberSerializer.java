package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.ConfigPrimitive;

import java.util.function.Function;

/**
 * A Serializer for numeric types
 * @param <T> The type of number to serialize
 */
public class NumberSerializer<T extends Number> implements Serializer<T> {

    private final Function<Number, T> converter;
    private final T lowerBound;
    private final T upperBound;

    /**
     * Creates a number serializer using the given converter and bounds
     * @param converter The function to use to convert Number objects to the serializer's number type
     * @param lowerValue The (inclusive) lower bound which will be considered valid
     * @param upperValue The (inclusive) upper bound which will be considered valid
     */
    public NumberSerializer(Function<Number, T> converter, T lowerValue, T upperValue) {
        this.converter = converter;
        this.lowerBound = lowerValue;
        this.upperBound = upperValue;
    }

    private SerializeResult<T> validate(Number value) {

        if(value == null) return SerializeResult.failure("Unable to validate null number!");

        if(ConfigPrimitive.isInteger(value)) {
            long lng = value.longValue();
            if(lng < lowerBound.longValue() || lng > upperBound.longValue()) {
                return SerializeResult.failure("Value " + value + " is outside of bound [" + lowerBound + "," + upperBound + "]");
            }
        } else {
            double dbl = value.doubleValue();
            if(dbl < lowerBound.doubleValue() || dbl > upperBound.doubleValue()) {
                return SerializeResult.failure("Value " + value + " is outside of bound [" + lowerBound + "," + upperBound + "]");
            }
        }
        return SerializeResult.success(converter.apply(value));
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {

        return validate(value).flatMap(context::toNumber);
    }

    @Override
    public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {

        return context.asNumber(value).map(this::validate);
    }

    /**
     * Creates a byte serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Byte> forByte(Byte lowerBound, Byte upperBound) {
        return new NumberSerializer<>(Number::byteValue, lowerBound, upperBound);
    }

    /**
     * Creates a short serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Short> forShort(Short lowerBound, Short upperBound) {
        return new NumberSerializer<>(Number::shortValue, lowerBound, upperBound);
    }

    /**
     * Creates an integer serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Integer> forInt(Integer lowerBound, Integer upperBound) {
        return new NumberSerializer<>(Number::intValue, lowerBound, upperBound);
    }

    /**
     * Creates a long serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Long> forLong(Long lowerBound, Long upperBound) {
        return new NumberSerializer<>(Number::longValue, lowerBound, upperBound);
    }

    /**
     * Creates a float serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Float> forFloat(Float lowerBound, Float upperBound) {
        return new NumberSerializer<>(Number::floatValue, lowerBound, upperBound);
    }

    /**
     * Creates a double serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Double> forDouble(Double lowerBound, Double upperBound) {
        return new NumberSerializer<>(Number::doubleValue, lowerBound, upperBound);
    }


}
