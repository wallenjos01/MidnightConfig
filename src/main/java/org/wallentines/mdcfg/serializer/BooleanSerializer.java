package org.wallentines.mdcfg.serializer;

@SuppressWarnings("unused")
public class BooleanSerializer implements Serializer<Boolean> {


    private final BooleanSerializerType type;
    public BooleanSerializer(BooleanSerializerType type) {
        this.type = type;
    }


    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Boolean value) {

        String error = "Unable to save " + value + " as a boolean!";
        switch(type) {
            case BOOLEAN:
                return SerializeResult.ofNullable(context.toBoolean(value), error);
            case STRING:
                return SerializeResult.ofNullable(context.toString(value ? "true" : "false"), error);
            case NUMBER:
                return SerializeResult.ofNullable(context.toNumber(value ? 1 : 0), error);
        }
        return SerializeResult.failure(error);
    }

    @Override
    public <O> SerializeResult<Boolean> deserialize(SerializeContext<O> context, O value) {
        return SerializeResult.ofNullable(context.asBoolean(value))
                .mapError(() -> SerializeResult.ofNullable(context.asNumber(value))
                        .flatMap(num -> num.doubleValue() != 0.0))
                .mapError(() -> SerializeResult.ofNullable(context.asString(value), "Unable to read " + value + " as a boolean!")
                        .map(str -> {
                            boolean out;
                            if((out = str.equalsIgnoreCase("true")) || str.equalsIgnoreCase("false")) {
                                return SerializeResult.success(out);
                            }
                            return SerializeResult.failure("Unable to read " + str + " as a boolean!");
                        }));
    }

    public static final BooleanSerializer RAW = new BooleanSerializer(BooleanSerializerType.BOOLEAN);
    public static final BooleanSerializer STRING = new BooleanSerializer(BooleanSerializerType.STRING);
    public static final BooleanSerializer NUMBER = new BooleanSerializer(BooleanSerializerType.NUMBER);

    public enum BooleanSerializerType {
        BOOLEAN,
        STRING,
        NUMBER
    }

}
