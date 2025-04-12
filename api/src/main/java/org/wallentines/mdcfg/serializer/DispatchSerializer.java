package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Functions;

public class DispatchSerializer<K, V> implements Serializer<V> {

    private final Serializer<K> keySerializer;
    private final Functions.F2<SerializeContext<?>, ? super V, ForwardSerializer<V>> dispatcher;
    private final Functions.F2<SerializeContext<?>, ? super K, BackSerializer<? extends V>> backDispatcher;
    private final Functions.F2<SerializeContext<?>, ? super V, ? extends K> keyGetter;

    public DispatchSerializer(
            Serializer<K> keySerializer,
            Functions.F2<SerializeContext<?>, ? super V, ForwardSerializer<V>> dispatcher,
            Functions.F2<SerializeContext<?>, ? super K, BackSerializer<? extends V>> backDispatcher,
            Functions.F2<SerializeContext<?>, ? super V, ? extends K> keyGetter) {

        this.keySerializer = keySerializer;
        this.dispatcher = dispatcher;
        this.backDispatcher = backDispatcher;
        this.keyGetter = keyGetter;
    }

    public DispatchSerializer(
            Serializer<K> keySerializer,
            Functions.F2<SerializeContext<?>, ? super K, Serializer<? extends V>> dispatcher,
            Functions.F2<SerializeContext<?>, ? super V, ? extends K> keyGetter) {

        this.keySerializer = keySerializer;
        this.keyGetter = keyGetter;
        this.dispatcher = (ctx, v) -> {
            K key = keyGetter.apply(ctx, v);
            Serializer<? extends V> ser = dispatcher.apply(ctx, key);
            return generify(ser);
        };
        this.backDispatcher = dispatcher::apply;

    }

    @SuppressWarnings("unchecked")
    private static <V, Q extends V> ForwardSerializer<V> generify(ForwardSerializer<Q> serializer) {
        return serializer.mapForward(v -> {
            try {
                Q out = (Q) v;
                return SerializeResult.success(out);
            } catch (ClassCastException ex) {
                return SerializeResult.failure(ex);
            }
        });
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, V value) {

        K key = keyGetter.apply(context, value);

        SerializeResult<O> keyResult = keySerializer.serialize(context, key);
        if(!keyResult.isComplete()) return SerializeResult.failure("Unable to serialize key for " + value + "!", keyResult.getError());

        ForwardSerializer<V> valueSerializer = dispatcher.apply(context, value);
        if(valueSerializer == null) return SerializeResult.failure("Unable to find value serializer for " + key + "!");

        SerializeResult<O> valueResult = valueSerializer.serialize(context, value);
        if(!valueResult.isComplete()) return SerializeResult.failure("Unable to serialize value for " + key + "!", valueResult.getError());

        return SerializeResult.success(context.merge(keyResult.getOrNull(), valueResult.getOrNull()));
    }

    @Override
    public <O> SerializeResult<V> deserialize(SerializeContext<O> context, O value) {

        SerializeResult<K> keyResult = keySerializer.deserialize(context, value);
        if(!keyResult.isComplete()) return SerializeResult.failure("Unable to deserialize key!", keyResult.getError());

        K key = keyResult.getOrNull();
        BackSerializer<? extends V> valueSerializer = backDispatcher.apply(context, key);
        if(valueSerializer == null) return SerializeResult.failure("Unable to find value serializer for " + key + "!");

        return valueSerializer.deserialize(context, value).flatMap(v -> v);
    }
}
