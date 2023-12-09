package org.wallentines.mdcfg.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ListContextSerializer<T,C> implements ContextSerializer<Collection<T>,C> {


    private final ContextSerializer<T, C> base;
    private final Function<String, Boolean> onError;

    /**
     * Creates a new list serializer using the given serializer as a base
     * @param base The serializer to use to convert each value in the list
     */
    public ListContextSerializer(ContextSerializer<T, C> base) {
        this(base, str -> true);
    }

    /**
     * Creates a new list serializer using the given serializer as a base, which reports errors to the given function
     * @param base The serializer to use to convert each value in the list
     * @param onError A function to be called whenever serializing of an object fails. If it returns true,
     *                (de)serializing will be stopped with an error
     */
    public ListContextSerializer(ContextSerializer<T, C> base, Function<String, Boolean> onError) {
        this.base = base;
        this.onError = onError;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Collection<T> value, C ctx) {

        List<O> out = new ArrayList<>();
        for(T t : value) {
            SerializeResult<O> res = base.serialize(context, t, ctx);
            if(res.isComplete()) {
                out.add(res.getOrThrow());

            } else {
                if(onError.apply(res.getError())) return SerializeResult.failure("Unable to serialize value " + t + " into a list! " + res.getError());
            }
        }

        return SerializeResult.ofNullable(context.toList(out));
    }

    @Override
    public <O> SerializeResult<Collection<T>> deserialize(SerializeContext<O> context, O value, C ctx) {

        Collection<O> list = context.asList(value);
        if(list == null) return SerializeResult.failure("Unable to read " + value + " as a list!");

        List<T> out = new ArrayList<>();
        for(O o : list) {
            SerializeResult<T> res = base.deserialize(context, o, ctx);
            if(res.isComplete()) {
                out.add(res.getOrThrow());
            } else {
                if(onError.apply(res.getError())) return SerializeResult.failure("Unable to deserialize value " + o + " from a list! " + res.getError());
            }

        }
        return SerializeResult.success(out);
    }


}
