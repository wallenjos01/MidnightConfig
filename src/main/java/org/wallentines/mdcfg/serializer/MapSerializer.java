package org.wallentines.mdcfg.serializer;

import java.util.HashMap;
import java.util.Map;

public class MapSerializer<K, V> implements Serializer<Map<K, V>> {

    private final InlineSerializer<K> keySerializer;
    private final Serializer<V> valueSerializer;

    public MapSerializer(InlineSerializer<K> keySerializer, Serializer<V> valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Map<K, V> object) {

        Map<String, O> out = new HashMap<>();
        for(Map.Entry<K, V> ent : object.entrySet()) {
            String key = keySerializer.writeString(ent.getKey());
            if(key == null) return SerializeResult.failure("Unable to serialize key " + ent.getKey() + " as a String!");

            SerializeResult<O> valueResult = valueSerializer.serialize(context, ent.getValue());
            if(!valueResult.isComplete()) return valueResult;

            out.put(key, valueResult.getOrThrow());
        }

        return SerializeResult.ofNullable(context.toMap(out), "Unable to serialize map!");
    }

    @Override
    public <O> SerializeResult<Map<K, V>> deserialize(SerializeContext<O> context, O object) {

        Map<K, V> out = new HashMap<>();
        Map<String, O> in = context.asMap(object);
        if(in == null) return SerializeResult.failure("Unable to convert " + object + " to a map!");

        for(Map.Entry<String, O> entry : in.entrySet()) {

            K key = keySerializer.readString(entry.getKey());
            if(key == null) return SerializeResult.failure("Unable to convert deserialize map key " + entry.getKey() + "!");

            SerializeResult<V> valueResult = valueSerializer.deserialize(context, entry.getValue());
            if(!valueResult.isComplete()) return SerializeResult.failure("Unable to deserialize map value " + entry.getValue() + " with key " + key + "! " + valueResult.getError());

            out.put(key, valueResult.getOrThrow());
        }

        return SerializeResult.success(out);
    }
}
