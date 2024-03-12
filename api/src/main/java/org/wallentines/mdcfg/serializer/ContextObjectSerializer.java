package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Functions;
import org.wallentines.mdcfg.Tuples;

import java.util.*;
import java.util.function.Function;

/**
 * A utility for easily creating complex context serializers for objects
 */
public class ContextObjectSerializer<T,C> implements ContextSerializer<T,C> {


    private final List<ContextEntry<?, T, C>> entries;
    private final Functions.F1<ObjectSerializer.EntrySet, SerializeResult<T>> constructor;

    public ContextObjectSerializer(Collection<ContextEntry<?, T, C>> entries, Functions.F1<ObjectSerializer.EntrySet, SerializeResult<T>> constructor) {
        this.entries = List.copyOf(entries);
        this.constructor = constructor;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> serializeContext, T value, C context) {
        Map<String, O> out = new HashMap<>();
        for(ContextEntry<?, T, C> entry : entries) {

            SerializeResult<Tuples.T2<String, O>> res = entry.resolve(serializeContext, value, context);
            if(!res.isComplete()) {
                return SerializeResult.failure(res.getError());
            }

            Tuples.T2<String, O> t = res.getOrThrow();
            out.put(t.p1, t.p2);
        }
        return SerializeResult.success(serializeContext.toMap(out));
    }

    @Override
    public <O> SerializeResult<T> deserialize(SerializeContext<O> serializeContext, O value, C context) {

        ObjectSerializer.EntrySet set = new ObjectSerializer.EntrySet();
        for(ContextEntry<?, T, C> ent : entries) {
            SerializeResult<?> res = ent.parse(serializeContext, value, context);
            if(!res.isComplete()) {
                return SerializeResult.failure(res.getError());
            }
            set.add(res.getOrThrow());
        }

        return constructor.apply(set);
    }


    public static class ContextEntry<T, O, C> {

        private final ContextSerializer<T, C> serializer;
        private final Functions.F2<O, C, T> getter;
        private final String key;
        private final List<String> alternateKeys = new ArrayList<>();
        private Function<C, T> defaultGetter;
        private boolean optional;

        public ContextEntry(ContextSerializer<T, C> serializer, Functions.F2<O, C, T> getter, String key) {
            this.serializer = serializer;
            this.getter = getter;
            this.key = key;
        }

        public ContextEntry(ContextSerializer<T, C> serializer, Functions.F2<O, C, T> getter, String key, Collection<String> alternateKeys) {
            this.serializer = serializer;
            this.getter = getter;
            this.key = key;
            this.alternateKeys.addAll(alternateKeys);
        }

        public ContextSerializer<T, C> getSerializer() {
            return serializer;
        }

        public T getValue(O object, C context) {
            return getter.apply(object, context);
        }

        public String getKey() {
            return key;
        }

        public ContextEntry<T, O, C> optional() {
            this.optional = true;
            return this;
        }

        public ContextEntry<T, O, C> orElse(Function<C, T> defaultGetter) {
            this.optional = true;
            this.defaultGetter = defaultGetter;
            return this;
        }

        public ContextEntry<T, O, C> acceptKey(String alternateKey) {
            this.alternateKeys.add(alternateKey);
            return this;
        }

        public <SC> SerializeResult<T> parse(SerializeContext<SC> serializeContext, SC value, C context) {

            SC val = serializeContext.get(key, value);
            if(val == null) {
                for(String s : alternateKeys) {
                    val = serializeContext.get(s, value);
                    if(val != null) break;
                }
            }

            if(val != null) {

                return serializer.deserialize(serializeContext, val, context).mapError(error -> SerializeResult.failure("Unable to deserialize value with key " + key + "! " + error));
            }
            if(!optional) {
                return SerializeResult.failure("Unable to find value for required key " + key + "!");
            }

            return SerializeResult.success(defaultGetter == null ? null : defaultGetter.apply(context));
        }

        public <SC> SerializeResult<Tuples.T2<String, SC>> resolve(SerializeContext<SC> serializeContext, O object, C context) {

            T out = getValue(object, context);
            if(out == null) {
                if(!optional) {
                    return SerializeResult.failure("A value for " + key + " could not be obtained from object!");
                }
                if(defaultGetter != null) {
                    return SerializeResult.success(new Tuples.T2<>(key, serializer.serialize(serializeContext, defaultGetter.apply(context), context).getOrThrow()));
                }

                return SerializeResult.success(new Tuples.T2<>(key, null));
            }

            return SerializeResult.success(new Tuples.T2<>(key, serializer.serialize(serializeContext, out, context).getOrThrow()));
        }
    }
    
    public static class Builder<O, C> {

        private final List<ContextEntry<?, O, C>> entries = new ArrayList<>();

        public <T> Builder<O, C> withEntry(ContextEntry<T, O, C> entry) {
            entries.add(entry);
            return this;
        }

        public ContextObjectSerializer<O, C> build(Functions.F1<ObjectSerializer.EntrySet, SerializeResult<O>> constructor) {
            return new ContextObjectSerializer<>(entries, constructor);
        }

    }

    public static <O, C> Builder<O,C> builder() {
        return new Builder<>();
    }


    public static <T,O,C> ContextEntry<T,O,C> entry(String key, ContextSerializer<T, C> serializer, Functions.F2<O,C,T> getter) {
        return new ContextEntry<>(serializer, getter, key);
    }

    public static <T,O,C> ContextEntry<T,O,C> entry(String key, Serializer<T> serializer, Functions.F2<O,C,T> getter) {
        return new ContextEntry<>(ContextSerializer.fromStatic(serializer), getter, key);
    }
    
    
    public static <T,P1,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, Functions.F1<P1,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0))));
    }

    public static <T,P1,P2,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, Functions.F2<P1,P2,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1))));
    }

    public static <T,P1,P2,P3,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, Functions.F3<P1,P2,P3,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2))));
    }

    public static <T,P1,P2,P3,P4,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, Functions.F4<P1,P2,P3,P4,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3))));
    }

    public static <T,P1,P2,P3,P4,P5,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, Functions.F5<P1,P2,P3,P4,P5,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, Functions.F6<P1,P2,P3,P4,P5,P6,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, Functions.F7<P1,P2,P3,P4,P5,P6,P7,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, Functions.F8<P1,P2,P3,P4,P5,P6,P7,P8,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, Functions.F9<P1,P2,P3,P4,P5,P6,P7,P8,P9,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, ContextEntry<P10, T, C> ent10, Functions.F10<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .withEntry(ent10)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8), set.get(10))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, ContextEntry<P10, T, C> ent10, ContextEntry<P11, T, C> ent11, Functions.F11<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .withEntry(ent10)
                .withEntry(ent11)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8), set.get(10), set.get(11))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, ContextEntry<P10, T, C> ent10, ContextEntry<P11, T, C> ent11, ContextEntry<P12, T, C> ent12, Functions.F12<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .withEntry(ent10)
                .withEntry(ent11)
                .withEntry(ent12)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8), set.get(10), set.get(11), set.get(12))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, ContextEntry<P10, T, C> ent10, ContextEntry<P11, T, C> ent11, ContextEntry<P12, T, C> ent12, ContextEntry<P13, T, C> ent13, Functions.F13<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .withEntry(ent10)
                .withEntry(ent11)
                .withEntry(ent12)
                .withEntry(ent13)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8), set.get(10), set.get(11), set.get(12), set.get(13))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, ContextEntry<P10, T, C> ent10, ContextEntry<P11, T, C> ent11, ContextEntry<P12, T, C> ent12, ContextEntry<P13, T, C> ent13, ContextEntry<P14, T, C> ent14, Functions.F14<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .withEntry(ent10)
                .withEntry(ent11)
                .withEntry(ent12)
                .withEntry(ent13)
                .withEntry(ent14)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8), set.get(10), set.get(11), set.get(12), set.get(13), set.get(14))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,C> ContextSerializer<T,C> create(ContextEntry<P1, T, C> ent1, ContextEntry<P2, T, C> ent2, ContextEntry<P3, T, C> ent3, ContextEntry<P4, T, C> ent4, ContextEntry<P5, T, C> ent5, ContextEntry<P6, T, C> ent6, ContextEntry<P7, T, C> ent7, ContextEntry<P8, T, C> ent8, ContextEntry<P9, T, C> ent9, ContextEntry<P10, T, C> ent10, ContextEntry<P11, T, C> ent11, ContextEntry<P12, T, C> ent12, ContextEntry<P13, T, C> ent13, ContextEntry<P14, T, C> ent14, ContextEntry<P15, T, C> ent15, Functions.F15<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,T> constructor) {

        return new Builder<T,C>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .withEntry(ent8)
                .withEntry(ent9)
                .withEntry(ent10)
                .withEntry(ent11)
                .withEntry(ent12)
                .withEntry(ent13)
                .withEntry(ent14)
                .withEntry(ent15)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6), set.get(7), set.get(8), set.get(10), set.get(11), set.get(12), set.get(13), set.get(14), set.get(15))));
    }

}
