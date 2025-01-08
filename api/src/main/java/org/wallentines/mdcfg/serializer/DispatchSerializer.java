package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Functions;

public class DispatchSerializer<K, V> implements Serializer<V> {

    private final Serializer<K> keySerializer;
    private final Functions.F2<SerializeContext<?>, K, Serializer<V>> dispatcher;
    private final Functions.F2<SerializeContext<?>, V, K> keyGetter;

    public DispatchSerializer(Serializer<K> keySerializer, Functions.F2<SerializeContext<?>, K, Serializer<V>> dispatcher, Functions.F2<SerializeContext<?>, V, K> keyGetter) {
        this.keySerializer = keySerializer;
        this.dispatcher = dispatcher;
        this.keyGetter = keyGetter;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, V value) {

        K key = keyGetter.apply(context, value);

        SerializeResult<O> keyResult = keySerializer.serialize(context, key);
        if(!keyResult.isComplete()) return SerializeResult.failure("Unable to serialize key for " + value + "!", keyResult.getError());

        Serializer<V> valueSerializer = dispatcher.apply(context, key);
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
        Serializer<V> valueSerializer = dispatcher.apply(context, key);
        if(valueSerializer == null) return SerializeResult.failure("Unable to find value serializer for " + key + "!");

        return valueSerializer.deserialize(context, value);
    }
}
