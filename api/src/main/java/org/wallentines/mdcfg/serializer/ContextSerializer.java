package org.wallentines.mdcfg.serializer;

/**
 * A context-aware serializer. A serializer which expects an object of a specific type to be available while (de)serializing
 * @param <T> The type of objects to serialize
 * @param <C> The context which should be present
 */
public interface ContextSerializer<T,C> {

    <O> SerializeResult<O> serialize(SerializeContext<O> serializeContext, T value, C context);

    <O> SerializeResult<T> deserialize(SerializeContext<O> serializeContext, O value, C context);


    default Serializer<T> forContext(C context) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> ctx, T value) {
                return ContextSerializer.this.serialize(ctx, value, context);
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> ctx, O value) {
                return ContextSerializer.this.deserialize(ctx, value, context);
            }
        };
    }

}
