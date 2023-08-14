package org.wallentines.mdcfg.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A serializer for lists of objects
 * @param <T> The type of objects to serialize
 */
public class ListSerializer<T> implements Serializer<Collection<T>> {

    private final Serializer<T> base;
    private final Function<String, Boolean> onError;

    /**
     * Creates a new list serializer using the given serializer as a base
     * @param base The serializer to use to convert each value in the list
     */
    public ListSerializer(Serializer<T> base) {
        this(base, str -> true);
    }

    /**
     * Creates a new list serializer using the given serializer as a base, which reports errors to the given function
     * @param base The serializer to use to convert each value in the list
     * @param onError A function to be called whenever serializing of an object fails. If it returns true,
     *                (de)serializing will be stopped with an error
     */
    public ListSerializer(Serializer<T> base, Function<String, Boolean> onError) {
        this.base = base;
        this.onError = onError;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Collection<T> value) {

        List<O> out = new ArrayList<>();
        for(T t : value) {
            SerializeResult<O> res = base.serialize(context, t);
            if(res.isComplete()) {
                out.add(res.getOrThrow());

            } else {
                if(onError.apply(res.getError())) return SerializeResult.failure("Unable to serialize value " + t + " into a list! " + res.getError());
            }
        }

        return SerializeResult.ofNullable(context.toList(out));
    }

    @Override
    public <O> SerializeResult<Collection<T>> deserialize(SerializeContext<O> context, O value) {

        Collection<O> list = context.asList(value);
        if(list == null) return SerializeResult.failure("Unable to read " + value + " as a list!");

        List<T> out = new ArrayList<>();
        for(O o : list) {
            SerializeResult<T> res = base.deserialize(context, o);
            if(res.isComplete()) {
                out.add(res.getOrThrow());
            } else {
                if(onError.apply(res.getError())) return SerializeResult.failure("Unable to deserialize value " + o + " from a list! " + res.getError());
            }

        }
        return SerializeResult.success(out);
    }
}
