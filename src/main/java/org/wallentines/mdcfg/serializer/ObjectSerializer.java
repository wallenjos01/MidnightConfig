package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Functions;
import org.wallentines.mdcfg.Tuples;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ObjectSerializer {

    public static class Entry<T, O> {

        private final Serializer<T> serializer;
        private final Function<O, T> getter;
        private final String key;
        private T defaultValue;
        private boolean optional;

        public Entry(Serializer<T> serializer, Function<O, T> getter, String key) {
            this.serializer = serializer;
            this.getter = getter;
            this.key = key;
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

        public <C> SerializeResult<T> parse(SerializeContext<C> context, C value) {

            C val = context.get(key, value);
            if(val != null) {

                return serializer.deserialize(context, val);
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

    private static <O> SerializeResult<Map<String, O>> saveEntries(Collection<SerializeResult<Tuples.T2<String, O>>> entries) {
        Map<String, O> out = new HashMap<>();
        for(SerializeResult<Tuples.T2<String, O>> entry : entries) {

            if(!entry.isComplete()) {
                return SerializeResult.failure(entry.getError());
            }

            Tuples.T2<String, O> value = entry.getOrThrow();
            out.put(value.p1, value.p2);
        }
        return SerializeResult.success(out);
    }

    public static <T, O> Entry<T, O> entry(String key, Serializer<T> serializer, Function<O, T> getter) {
        return new Entry<>(serializer, getter, key);
    }

    public static <T,P1> Serializer<T> create(Entry<P1, T> ent1, Functions.F1<P1,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value))).map(map -> SerializeResult.success(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).flatMap(constructor::apply);
            }
        };
    }

    public static <T,P1,P2> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Functions.F2<P1,P2,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2));
            }
        };
    }

    public static <T,P1,P2,P3> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Functions.F3<P1,P2,P3,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3));
            }
        };
    }

    public static <T,P1,P2,P3,P4> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Functions.F4<P1,P2,P3,P4,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Functions.F5<P1,P2,P3,P4,P5,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Functions.F6<P1,P2,P3,P4,P5,P6,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Functions.F7<P1,P2,P3,P4,P5,P6,P7,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Functions.F8<P1,P2,P3,P4,P5,P6,P7,P8,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Functions.F9<P1,P2,P3,P4,P5,P6,P7,P8,P9,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Functions.F10<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value), ent10.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value), ent10.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9, tp.p10));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Functions.F11<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value), ent10.resolve(context, value), ent11.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value), ent10.parse(context, value), ent11.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9, tp.p10, tp.p11));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Functions.F12<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value), ent10.resolve(context, value), ent11.resolve(context, value), ent12.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value), ent10.parse(context, value), ent11.parse(context, value), ent12.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9, tp.p10, tp.p11, tp.p12));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Entry<P13, T> ent13, Functions.F13<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value), ent10.resolve(context, value), ent11.resolve(context, value), ent12.resolve(context, value), ent13.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value), ent10.parse(context, value), ent11.parse(context, value), ent12.parse(context, value), ent13.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9, tp.p10, tp.p11, tp.p12, tp.p13));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Entry<P13, T> ent13, Entry<P14, T> ent14, Functions.F14<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value), ent10.resolve(context, value), ent11.resolve(context, value), ent12.resolve(context, value), ent13.resolve(context, value), ent14.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value), ent10.parse(context, value), ent11.parse(context, value), ent12.parse(context, value), ent13.parse(context, value), ent14.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9, tp.p10, tp.p11, tp.p12, tp.p13, tp.p14));
            }
        };
    }

    public static <T,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15> Serializer<T> create(Entry<P1, T> ent1, Entry<P2, T> ent2, Entry<P3, T> ent3, Entry<P4, T> ent4, Entry<P5, T> ent5, Entry<P6, T> ent6, Entry<P7, T> ent7, Entry<P8, T> ent8, Entry<P9, T> ent9, Entry<P10, T> ent10, Entry<P11, T> ent11, Entry<P12, T> ent12, Entry<P13, T> ent13, Entry<P14, T> ent14, Entry<P15, T> ent15, Functions.F15<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,T> constructor) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value) {
                return saveEntries(List.of(ent1.resolve(context, value), ent2.resolve(context, value), ent3.resolve(context, value), ent4.resolve(context, value), ent5.resolve(context, value), ent6.resolve(context, value), ent7.resolve(context, value), ent8.resolve(context, value), ent9.resolve(context, value), ent10.resolve(context, value), ent11.resolve(context, value), ent12.resolve(context, value), ent13.resolve(context, value), ent14.resolve(context, value), ent15.resolve(context, value))).map(map -> SerializeResult.ofNullable(context.toMap(map)));
            }
            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value) {
                return ent1.parse(context, value).and(ent2.parse(context, value), ent3.parse(context, value), ent4.parse(context, value), ent5.parse(context, value), ent6.parse(context, value), ent7.parse(context, value), ent8.parse(context, value), ent9.parse(context, value), ent10.parse(context, value), ent11.parse(context, value), ent12.parse(context, value), ent13.parse(context, value), ent14.parse(context, value), ent15.parse(context, value)).flatMap(tp -> constructor.apply(tp.p1, tp.p2, tp.p3, tp.p4, tp.p5, tp.p6, tp.p7, tp.p8, tp.p9, tp.p10, tp.p11, tp.p12, tp.p13, tp.p14, tp.p15));
            }
        };
    }
}
