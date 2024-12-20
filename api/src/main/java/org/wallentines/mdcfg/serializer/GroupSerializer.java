package org.wallentines.mdcfg.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GroupSerializer<T> implements Serializer<T> {

    private final List<Entry<T, ?>> entries;
    private final Function<GroupResult, SerializeResult<T>> constructor;

    protected GroupSerializer(List<Entry<T, ?>> entries, Function<GroupResult, SerializeResult<T>> constructor) {
        this.entries = entries;
        this.constructor = constructor;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {

        O out = null;
        for(Entry<T, ?> entry : entries) {
            SerializeResult<O> ent = entry.serialize(context, value);
            if(!ent.isComplete()) {
                return SerializeResult.failure(ent.getError());
            }

            if(out == null) {
                out = ent.getOrThrow();
            } else {
                out = context.merge(out, ent.getOrThrow());
            }
        }

        return SerializeResult.success(out);
    }

    @Override
    public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {

        List<Object> objects = new ArrayList<>();
        for(Entry<T, ?> entry : entries) {

            SerializeResult<?> res = entry.serializer.deserialize(context, value);
            if(!res.isComplete()) {
                return SerializeResult.failure(res.getError());
            }

            objects.add(res.getOrThrow());
        }

        GroupResult res = new GroupResult(objects);
        return constructor.apply(res);
    }

    protected static class Entry<T, E> {

        protected final Serializer<E> serializer;
        protected final Function<T, E> getter;

        protected Entry(Serializer<E> serializer, Function<T, E> getter) {
            this.serializer = serializer;
            this.getter = getter;
        }

        protected <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
            return serializer.serialize(context, getter.apply(value));
        }
    }


    public static class Builder<T> {

        private final List<Entry<T, ?>> entries = new ArrayList<>();

        public <E> Builder<T> add(Serializer<E> serializer, Function<T, E> getter) {
            entries.add(new Entry<>(serializer, getter));
            return this;
        }

        public GroupSerializer<T> build(Function<GroupResult, SerializeResult<T>> constructor) {
            return new GroupSerializer<>(entries, constructor);
        }

    }

}
