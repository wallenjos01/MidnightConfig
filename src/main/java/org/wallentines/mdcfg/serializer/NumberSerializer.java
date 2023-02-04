package org.wallentines.mdcfg.serializer;

import java.util.function.Function;

public class NumberSerializer<T extends Number> implements Serializer<T> {

    private final Function<Number, T> converter;
    private final T lowerBound;
    private final T upperBound;

    public NumberSerializer(Function<Number, T> converter, T lowerValue, T upperValue) {
        this.converter = converter;
        this.lowerBound = lowerValue;
        this.upperBound = upperValue;
    }

    private SerializeResult<T> validate(Number value) {

        if(value == null) return SerializeResult.failure("Unable to validate null number!");

        if(value.equals(value.longValue())) {
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

        return SerializeResult.ofNullable(context.asNumber(value), "Unable to read " + value + " as a number!").map(this::validate);
    }

    public static NumberSerializer<Byte> forByte(Byte lowerBound, Byte upperBound) {
        return new NumberSerializer<>(Number::byteValue, lowerBound, upperBound);
    }
    public static NumberSerializer<Short> forShort(Short lowerBound, Short upperBound) {
        return new NumberSerializer<>(Number::shortValue, lowerBound, upperBound);
    }
    public static NumberSerializer<Integer> forInt(Integer lowerBound, Integer upperBound) {
        return new NumberSerializer<>(Number::intValue, lowerBound, upperBound);
    }
    public static NumberSerializer<Long> forLong(Long lowerBound, Long upperBound) {
        return new NumberSerializer<>(Number::longValue, lowerBound, upperBound);
    }
    public static NumberSerializer<Float> forFloat(Float lowerBound, Float upperBound) {
        return new NumberSerializer<>(Number::floatValue, lowerBound, upperBound);
    }
    public static NumberSerializer<Double> forDouble(Double lowerBound, Double upperBound) {
        return new NumberSerializer<>(Number::doubleValue, lowerBound, upperBound);
    }


}
