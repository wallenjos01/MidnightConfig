package org.wallentines.mdcfg.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ListSerializer<T> implements Serializer<Collection<T>> {

    private final Serializer<T> base;
    private final Consumer<String> onError;

    public ListSerializer(Serializer<T> base) {
        this(base, null);
    }

    public ListSerializer(Serializer<T> base, Consumer<String> onError) {
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
                if(onError == null) return SerializeResult.failure("Unable to serialize value " + t + " into a list! " + res.getError());
                onError.accept(res.getError());
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
                if(onError == null) return SerializeResult.failure("Unable to deserialize value " + o + " from a list! " + res.getError());
                onError.accept(res.getError());
            }

        }
        return SerializeResult.success(out);
    }
}
