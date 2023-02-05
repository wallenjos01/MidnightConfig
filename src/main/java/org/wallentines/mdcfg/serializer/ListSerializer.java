package org.wallentines.mdcfg.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListSerializer<T> implements Serializer<Collection<T>> {

    private final Serializer<T> base;

    public ListSerializer(Serializer<T> base) {
        this.base = base;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Collection<T> value) {

        List<O> out = new ArrayList<>();
        for(T t : value) {
            SerializeResult<O> res = base.serialize(context, t);
            if(!res.isComplete()) return res;
            out.add(res.getOrThrow());
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
            if(!res.isComplete()) return SerializeResult.failure(res.getError());
            out.add(res.getOrThrow());
        }
        return SerializeResult.success(out);
    }
}
