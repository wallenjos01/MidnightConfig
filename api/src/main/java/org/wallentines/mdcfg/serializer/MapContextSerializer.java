package org.wallentines.mdcfg.serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MapContextSerializer<K, V, C> implements ContextSerializer<Map<K, V>, C> {


    private final InlineContextSerializer<K, C> keySerializer;
    private final ContextSerializer<V, C> valueSerializer;
    private final BiFunction<K, Throwable, Boolean> onError;

    /**
     * Creates a MapSerializer with the given key and value serializers
     * @param keySerializer The serializer to use to serialize keys
     * @param valueSerializer The serializer to use to map values
     */
    public MapContextSerializer(InlineContextSerializer<K, C> keySerializer, ContextSerializer<V, C> valueSerializer) {
        this(keySerializer, valueSerializer, (k, str) -> true);
    }

    /**
     * Creates a MapSerializer with the given key and value serializers, which reports errors to the given function
     * @param keySerializer The serializer to use to serialize keys
     * @param valueSerializer The serializer to use to map values
     * @param onError The function to call when an error is encountered. If this function returns true, (de)serializing
     *                will be stopped with an error
     */
    @Deprecated
    public MapContextSerializer(InlineContextSerializer<K, C> keySerializer, ContextSerializer<V, C> valueSerializer, Function<Throwable, Boolean> onError) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.onError = (k, str) -> onError.apply(str);
    }
    /**
     * Creates a MapSerializer with the given key and value serializers, which reports errors to the given function
     * @param keySerializer The serializer to use to serialize keys
     * @param valueSerializer The serializer to use to map values
     * @param onError The function to call when an error is encountered. If this function returns true, (de)serializing
     *                will be stopped with an error
     */
    public MapContextSerializer(InlineContextSerializer<K, C> keySerializer, ContextSerializer<V, C> valueSerializer, BiFunction<K, Throwable, Boolean> onError) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.onError = onError;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Map<K, V> object, C ctx) {

        Map<String, O> out = new HashMap<>();
        for(Map.Entry<K, V> ent : object.entrySet()) {
            String key = keySerializer.writeString(ent.getKey(), ctx);
            if(key == null) return SerializeResult.failure("Unable to serialize key " + ent.getKey() + " as a String!");

            SerializeResult<O> valueResult = valueSerializer.serialize(context, ent.getValue(), ctx);
            if(!valueResult.isComplete() && onError.apply(ent.getKey(), valueResult.getError())) {
                return valueResult;
            }

            if(valueResult.isComplete()) {
                out.put(key, valueResult.getOrThrow());
            }
        }

        return SerializeResult.ofNullable(context.toMap(out), "Unable to serialize map!");
    }

    @Override
    public <O> SerializeResult<Map<K, V>> deserialize(SerializeContext<O> context, O object, C ctx) {

        return context.asMap(object).map(in -> {

            Map<K, V> out = new HashMap<>();
            for(Map.Entry<String, O> entry : in.entrySet()) {

                K key = keySerializer.readString(entry.getKey(), ctx);
                if(key == null) return SerializeResult.failure("Unable to deserialize map key " + entry.getKey() + "!");

                SerializeResult<V> valueResult = valueSerializer.deserialize(context, entry.getValue(), ctx);
                if(!valueResult.isComplete() && onError.apply(key, valueResult.getError())) {
                    return SerializeResult.failure("Unable to deserialize map value " + entry.getValue() + " with key " + key + "! " + valueResult.getError());
                }

                if(valueResult.isComplete()) {
                    out.put(key, valueResult.getOrThrow());
                }
            }

            return SerializeResult.success(out);
        });
    }

}
