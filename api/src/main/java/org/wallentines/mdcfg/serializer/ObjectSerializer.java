package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Functions;
import org.wallentines.mdcfg.Tuples;

import java.util.*;
import java.util.function.Function;

/**
 * A utility for easily creating complex serializers for objects
 */
@SuppressWarnings("unused")
public class ObjectSerializer<T> implements Serializer<T> {

    private final List<Entry<?, T>> entries;
    private final Functions.F1<EntrySet, SerializeResult<T>> constructor;

    public ObjectSerializer(Collection<Entry<?, T>> entries, Functions.F1<EntrySet, SerializeResult<T>> constructor) {
        this.entries = List.copyOf(entries);
        this.constructor = constructor;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {

        Map<String, O> out = new HashMap<>();
        for(Entry<?, T> entry : entries) {

            SerializeResult<Tuples.T2<String, O>> res = entry.resolve(context, value);
            if(!res.isComplete()) {
                return SerializeResult.failure(res.getError());
            }

            Tuples.T2<String, O> t = res.getOrThrow();
            out.put(t.p1, t.p2);
        }
        return SerializeResult.success(context.toMap(out));
    }

    @Override
    public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {

        EntrySet set = new EntrySet();
        for(Entry<?, T> ent : entries) {
            SerializeResult<?> res = ent.parse(context, value);
            if(!res.isComplete()) {
                return SerializeResult.failure(res.getError());
            }
            set.add(res.getOrThrow());
        }

        return constructor.apply(set);
    }


    public static class EntrySet {

        private final List<Object> deserialized = new ArrayList<>();

        void add(Object o) {
            deserialized.add(o);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(int index) {
            return (T) deserialized.get(index);
        }
        public <T> T get(int index, Class<T> clazz) {
            Object out = deserialized.get(index);
            if(out == null) return null;
            if(out.getClass() == clazz || clazz.isAssignableFrom(out.getClass())) {
                return clazz.cast(out);
            }
            return null;
        }

    }

    public static class Entry<T, O> {

        private final Serializer<T> serializer;
        private final Function<O, T> getter;
        private final String key;
        private final List<String> alternateKeys = new ArrayList<>();
        private T defaultValue;
        private boolean optional;

        public Entry(Serializer<T> serializer, Function<O, T> getter, String key) {
            this.serializer = serializer;
            this.getter = getter;
            this.key = key;
        }

        public Entry(Serializer<T> serializer, Function<O, T> getter, String key, Collection<String> alternateKeys) {
            this.serializer = serializer;
            this.getter = getter;
            this.key = key;
            this.alternateKeys.addAll(alternateKeys);
        }

        public Serializer<T> getSerializer() {
            return serializer;
        }

        public T getValue(O object) {
            return getter.apply(object);
        }

        public String getKey() {
            return key;
        }

        public Entry<T, O> optional() {
            this.optional = true;
            return this;
        }

        public Entry<T, O> orElse(T defaultValue) {
            this.optional = true;
            this.defaultValue = defaultValue;
            return this;
        }

        public Entry<T, O> acceptKey(String alternateKey) {
            this.alternateKeys.add(alternateKey);
            return this;
        }

        public <C> SerializeResult<T> parse(SerializeContext<C> context, C value) {

            C val = context.get(key, value);
            if(val == null) {
                for(String s : alternateKeys) {
                    val = context.get(s, value);
                    if(val != null) break;
                }
            }

            if(val != null) {
                return serializer.deserialize(context, val).mapError(error -> SerializeResult.failure("Unable to deserialize value with key " + key + "! " + error));
            }
            if(!optional) {
                return SerializeResult.failure("Unable to find value for required key " + key + "!");
            }

            return SerializeResult.success(defaultValue);
        }

        public <C> SerializeResult<Tuples.T2<String, C>> resolve(SerializeContext<C> context, O object) {

            T out = getValue(object);
            if(out == null) {
                if(!optional) {
                    return SerializeResult.failure("A value for " + key + " could not be obtained from object!");
                }
                if(defaultValue != null) {
                    return SerializeResult.success(new Tuples.T2<>(key, serializer.serialize(context, defaultValue).getOrThrow()));
                }

                return SerializeResult.success(new Tuples.T2<>(key, null));
            }

            return SerializeResult.success(new Tuples.T2<>(key, serializer.serialize(context, out).getOrThrow()));
        }
    }


    public static class Builder<O> {

        private final List<Entry<?, O>> entries = new ArrayList<>();

        public <T> Builder<O> withEntry(Entry<T, O> entry) {
            entries.add(entry);
            return this;
        }

        public ObjectSerializer<O> build(Functions.F1<EntrySet, SerializeResult<O>> constructor) {
            return new ObjectSerializer<O>(entries, constructor);
        }

    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T, O> Entry<T, O> entry(String key, Serializer<T> serializer, Function<O, T> getter) {
        return new Entry<>(serializer, getter, key);
    }

    public static <T,P1> Serializer<T> create(Entry<P1, T> ent1, Functions.F1<P1,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0))));
    }

    public static <T,P1,P2> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Functions.F2<P1,P2,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .withEntry(ent2)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1))));
    }

    public static <T,P1,P2,P3> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Functions.F3<P1,P2,P3,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2))));
    }

    public static <T,P1,P2,P3,P4> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Functions.F4<P1,P2,P3,P4,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3))));
    }

    public static <T,P1,P2,P3,P4,P5> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Functions.F5<P1,P2,P3,P4,P5,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4))));
    }

    public static <T,P1,P2,P3,P4,P5,P6> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Functions.F6<P1,P2,P3,P4,P5,P6,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Functions.F7<P1,P2,P3,P4,P5,P6,P7,T> constructor) {

        return new Builder<T>()
                .withEntry(ent1)
                .withEntry(ent2)
                .withEntry(ent3)
                .withEntry(ent4)
                .withEntry(ent5)
                .withEntry(ent6)
                .withEntry(ent7)
                .build(set -> SerializeResult.success(constructor.apply(set.get(0), set.get(1), set.get(2), set.get(3), set.get(4), set.get(5), set.get(6))));
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Functions.F8<P1,P2,P3,P4,P5,P6,P7,P8,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Functions.F9<P1,P2,P3,P4,P5,P6,P7,P8,P9,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Functions.F10<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Functions.F11<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Functions.F12<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Entry<P13, T> ent13, Functions.F13<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Entry<P13, T> ent13, Entry<P14, T> ent14, Functions.F14<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,T> constructor) {

        return new Builder<T>()
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

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Entry<P13, T> ent13, Entry<P14, T> ent14, Entry<P15, T> ent15, Functions.F15<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,T> constructor) {

        return new Builder<T>()
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
