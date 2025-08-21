package org.wallentines.mdcfg.serializer;

import java.util.function.Function;
import org.wallentines.mdcfg.ConfigPrimitive;

/**
 * A Serializer for numeric types
 * @param <T> The type of number to serialize
 */
public class NumberSerializer<T extends Number> implements Serializer<T> {

    private final Function<Number, T> converter;
    private final T lowerBound;
    private final T upperBound;
    private final Function<String, SerializeResult<T>> parser;

    /**
     * Creates a number serializer using the given converter and bounds
     * @param converter The function to use to convert Number objects to the
     *     serializer's number type
     * @param lowerValue The (inclusive) lower bound which will be considered
     *     valid
     * @param upperValue The (inclusive) upper bound which will be considered
     *     valid
     */
    @Deprecated
    public NumberSerializer(Function<Number, T> converter, T lowerValue,
                            T upperValue) {
        this.converter = converter;
        this.lowerBound = lowerValue;
        this.upperBound = upperValue;
        this.parser = str -> {
            try {
                if (str.contains(".")) {
                    return SerializeResult.success(
                        converter.apply(Double.parseDouble(str)));
                } else {
                    return SerializeResult.success(
                        converter.apply(Long.parseLong(str)));
                }
            } catch (NumberFormatException ex) {
                return SerializeResult.failure(ex);
            }
        };
    }

    public NumberSerializer(Function<Number, T> converter, T lowerValue,
                            T upperValue, Function<String, T> parser) {

        this.converter = converter;
        this.lowerBound = lowerValue;
        this.upperBound = upperValue;
        this.parser = str -> {
            try {
                return SerializeResult.success(parser.apply(str));
            } catch (NumberFormatException ex) {
                return SerializeResult.failure(ex);
            }
        };
    }

    private SerializeResult<T> validate(Number value) {

        if (value == null)
            return SerializeResult.failure("Unable to validate null number!");

        if (ConfigPrimitive.isInteger(value)) {
            long lng = value.longValue();
            if (lng < lowerBound.longValue() || lng > upperBound.longValue()) {
                return SerializeResult.failure(
                    "Value " + value + " is outside of bound [" + lowerBound +
                    "," + upperBound + "]");
            }
        } else {
            double dbl = value.doubleValue();
            if (dbl < lowerBound.doubleValue() ||
                dbl > upperBound.doubleValue()) {
                return SerializeResult.failure(
                    "Value " + value + " is outside of bound [" + lowerBound +
                    "," + upperBound + "]");
            }
        }
        return SerializeResult.success(converter.apply(value));
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context,
                                            T value) {

        return validate(value).flatMap(context::toNumber);
    }

    @Override
    public <O> SerializeResult<T> deserialize(SerializeContext<O> context,
                                              O value) {

        return context.asNumber(value)
            .mapError(th -> context.asString(value).map(this.parser::apply))
            .map(this::validate);
    }

    @Override
    public Serializer<T> orEnv() {
        return orEnv(inline());
    }

    public InlineSerializer<T> inline() {

        return new InlineSerializer<T>() {
            @Override
            public <O> SerializeResult<T> readString(SerializeContext<O> ctx,
                                                     String str) {
                return parser.apply(str);
            }

            @Override
            public <O> SerializeResult<String> writeString(
                SerializeContext<O> ctx, T value) {
                if (value == null)
                    return SerializeResult.failure("Number was null!");
                return SerializeResult.success(value.toString());
            }
        };
    }

    /**
     * Creates a byte serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Byte> forByte(Byte lowerBound,
                                                 Byte upperBound) {
        return new NumberSerializer<>(Number::byteValue, lowerBound, upperBound,
                                      Byte::parseByte);
    }

    /**
     * Creates a short serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Short> forShort(Short lowerBound,
                                                   Short upperBound) {
        return new NumberSerializer<>(Number::shortValue, lowerBound,
                                      upperBound, Short::parseShort);
    }

    /**
     * Creates an integer serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Integer> forInt(Integer lowerBound,
                                                   Integer upperBound) {
        return new NumberSerializer<>(Number::intValue, lowerBound, upperBound,
                                      Integer::parseInt);
    }

    /**
     * Creates a long serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Long> forLong(Long lowerBound,
                                                 Long upperBound) {
        return new NumberSerializer<>(Number::longValue, lowerBound, upperBound,
                                      Long::parseLong);
    }

    /**
     * Creates a float serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Float> forFloat(Float lowerBound,
                                                   Float upperBound) {
        return new NumberSerializer<>(Number::floatValue, lowerBound,
                                      upperBound, Float::parseFloat);
    }

    /**
     * Creates a double serializer with the given lower and upper bounds
     * @param lowerBound The lower bound (inclusive)
     * @param upperBound The upper bound (inclusive)
     * @return A new number serializer
     */
    public static NumberSerializer<Double> forDouble(Double lowerBound,
                                                     Double upperBound) {
        return new NumberSerializer<>(Number::doubleValue, lowerBound,
                                      upperBound, Double::parseDouble);
    }
}
