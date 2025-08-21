package org.wallentines.mdcfg.serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A serializer for key-value pairs of objects
 * @param <K> The type of keys to serialize
 * @param <V> The type of values to serialize
 */
public class MapSerializer<K, V> implements Serializer<Map<K, V>> {

    private final InlineSerializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final BiFunction<K, Throwable, Boolean> onError;

    /**
     * Creates a MapSerializer with the given key and value serializers
     * @param keySerializer The serializer to use to serialize keys
     * @param valueSerializer The serializer to use to map values
     */
    public MapSerializer(InlineSerializer<K> keySerializer,
                         Serializer<V> valueSerializer) {
        this(keySerializer, valueSerializer, (k, str) -> true);
    }

    /**
     * Creates a MapSerializer with the given key and value serializers, which
     * reports errors to the given function
     * @param keySerializer The serializer to use to serialize keys
     * @param valueSerializer The serializer to use to map values
     * @param onError The function to call when an error is encountered. If this
     *     function returns true, (de)serializing
     *                will be stopped with an error
     */
    @Deprecated
    public MapSerializer(InlineSerializer<K> keySerializer,
                         Serializer<V> valueSerializer,
                         Function<Throwable, Boolean> onError) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.onError = (k, str) -> onError.apply(str);
    }
    /**
     * Creates a MapSerializer with the given key and value serializers, which
     * reports errors to the given function
     * @param keySerializer The serializer to use to serialize keys
     * @param valueSerializer The serializer to use to map values
     * @param onError The function to call when an error is encountered. If this
     *     function returns true, (de)serializing
     *                will be stopped with an error
     */
    public MapSerializer(InlineSerializer<K> keySerializer,
                         Serializer<V> valueSerializer,
                         BiFunction<K, Throwable, Boolean> onError) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.onError = onError;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context,
                                            Map<K, V> object) {

        Map<String, O> out = new HashMap<>();
        for (Map.Entry<K, V> ent : object.entrySet()) {
            SerializeResult<String> key =
                keySerializer.writeString(context, ent.getKey());
            if (!key.isComplete())
                return SerializeResult.failure("Unable to serialize key " +
                                               ent.getKey() + " as a String!");

            SerializeResult<O> valueResult =
                valueSerializer.serialize(context, ent.getValue());
            if (!valueResult.isComplete() &&
                onError.apply(ent.getKey(), valueResult.getError())) {
                return valueResult;
            }

            if (valueResult.isComplete()) {
                out.put(key.getOrThrow(), valueResult.getOrThrow());
            }
        }

        return SerializeResult.ofNullable(context.toMap(out),
                                          "Unable to serialize map!");
    }

    @Override
    public <O> SerializeResult<Map<K, V>>
    deserialize(SerializeContext<O> context, O object) {

        return context.asMap(object).map(in -> {
            Map<K, V> out = new HashMap<>();
            for (Map.Entry<String, O> entry : in.entrySet()) {

                SerializeResult<K> key =
                    keySerializer.readString(context, entry.getKey());
                if (!key.isComplete())
                    return SerializeResult.failure(
                        "Unable to deserialize map key " + entry.getKey() +
                        "!");

                SerializeResult<V> valueResult =
                    valueSerializer.deserialize(context, entry.getValue());
                if (!valueResult.isComplete() &&
                    onError.apply(key.getOrNull(), valueResult.getError())) {
                    return SerializeResult.failure(
                        "Unable to deserialize map value " + entry.getValue() +
                        " with key " + key + "! " + valueResult.getError());
                }

                if (valueResult.isComplete()) {
                    out.put(key.getOrNull(), valueResult.getOrThrow());
                }
            }

            return SerializeResult.success(out);
        });
    }
}
