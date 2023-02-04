package org.wallentines.mdcfg.serializer;

import org.wallentines.mdcfg.Tuples;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SerializeResult<T> {


    private final T value;
    private final String error;
    private final boolean success;

    private SerializeResult(T value, String error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    public static <T> SerializeResult<T> success(T value) {
        return new SerializeResult<>(value, null, true);
    }

    public static <T> SerializeResult<T> failure(String error) {
        return new SerializeResult<>(null, error, false);
    }

    public static <T> SerializeResult<T> ofNullable(T value) {
        return ofNullable(value, "Value was null!");
    }

    public static <T> SerializeResult<T> ofNullable(T value, String error) {
        if(value == null) {
            return failure(error);
        }
        return success(value);
    }

    public boolean isComplete() {
        return success;
    }

    public String getError() {
        return error;
    }

    public Optional<T> get() {
        return Optional.ofNullable(value);
    }

    public T getOrThrow() {
        if(!success) throw new SerializeException(error);
        return value;
    }

    public <O> SerializeResult<O> map(Function<T, SerializeResult<O>> converter) {
        if(!success) return SerializeResult.failure(error);
        return converter.apply(value);
    }

    public <O> SerializeResult<O> flatMap(Function<T, O> converter) {
        if(!success) return SerializeResult.failure(error);
        return SerializeResult.ofNullable(converter.apply(value));
    }

    public SerializeResult<T> mapError(Supplier<SerializeResult<T>> supplier) {
        if(!success) return supplier.get();
        return this;
    }

    public <O1> SerializeResult<Tuples.T2<T,O1>> and(SerializeResult<O1> o1) {
        return anyFail(() -> new Tuples.T2<>(value, o1.value), this, o1);
    }

    public <O1,O2> SerializeResult<Tuples.T3<T,O1,O2>> and(SerializeResult<O1> o1, SerializeResult<O2> o2) {
        return anyFail(() -> new Tuples.T3<>(value, o1.value, o2.value), this, o1, o2);
    }

    public <O1,O2,O3> SerializeResult<Tuples.T4<T,O1,O2,O3>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3) {
        return anyFail(() -> new Tuples.T4<>(value, o1.value, o2.value, o3.value), this, o1, o2, o3);
    }

    public <O1,O2,O3,O4> SerializeResult<Tuples.T5<T,O1,O2,O3,O4>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4) {
        return anyFail(() -> new Tuples.T5<>(value, o1.value, o2.value, o3.value, o4.value), this, o1, o2, o3, o4);
    }

    public <O1,O2,O3,O4,O5> SerializeResult<Tuples.T6<T,O1,O2,O3,O4,O5>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5) {
        return anyFail(() -> new Tuples.T6<>(value, o1.value, o2.value, o3.value, o4.value, o5.value), this, o1, o2, o3, o4, o5);
    }

    public <O1,O2,O3,O4,O5,O6> SerializeResult<Tuples.T7<T,O1,O2,O3,O4,O5,O6>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6) {
        return anyFail(() -> new Tuples.T7<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value), this, o1, o2, o3, o4, o5, o6);
    }

    public <O1,O2,O3,O4,O5,O6,O7> SerializeResult<Tuples.T8<T,O1,O2,O3,O4,O5,O6,O7>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7) {
        return anyFail(() -> new Tuples.T8<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value), this, o1, o2, o3, o4, o5, o6, o7);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8> SerializeResult<Tuples.T9<T,O1,O2,O3,O4,O5,O6,O7,O8>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8) {
        return anyFail(() -> new Tuples.T9<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value), this, o1, o2, o3, o4, o5, o6, o7, o8);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8,O9> SerializeResult<Tuples.T10<T,O1,O2,O3,O4,O5,O6,O7,O8,O9>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8, SerializeResult<O9> o9) {
        return anyFail(() -> new Tuples.T10<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value, o9.value), this, o1, o2, o3, o4, o5, o6, o7, o8, o9);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8,O9,O10> SerializeResult<Tuples.T11<T,O1,O2,O3,O4,O5,O6,O7,O8,O9,O10>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8, SerializeResult<O9> o9, SerializeResult<O10> o10) {
        return anyFail(() -> new Tuples.T11<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value, o9.value, o10.value), this, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11> SerializeResult<Tuples.T12<T,O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8, SerializeResult<O9> o9, SerializeResult<O10> o10, SerializeResult<O11> o11) {
        return anyFail(() -> new Tuples.T12<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value, o9.value, o10.value, o11.value), this, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11,O12> SerializeResult<Tuples.T13<T,O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11,O12>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8, SerializeResult<O9> o9, SerializeResult<O10> o10, SerializeResult<O11> o11, SerializeResult<O12> o12) {
        return anyFail(() -> new Tuples.T13<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value, o9.value, o10.value, o11.value, o12.value), this, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11,O12,O13> SerializeResult<Tuples.T14<T,O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11,O12,O13>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8, SerializeResult<O9> o9, SerializeResult<O10> o10, SerializeResult<O11> o11, SerializeResult<O12> o12, SerializeResult<O13> o13) {
        return anyFail(() -> new Tuples.T14<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value, o9.value, o10.value, o11.value, o12.value, o13.value), this, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13);
    }

    public <O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11,O12,O13,O14> SerializeResult<Tuples.T15<T,O1,O2,O3,O4,O5,O6,O7,O8,O9,O10,O11,O12,O13,O14>> and(SerializeResult<O1> o1, SerializeResult<O2> o2, SerializeResult<O3> o3, SerializeResult<O4> o4, SerializeResult<O5> o5, SerializeResult<O6> o6, SerializeResult<O7> o7, SerializeResult<O8> o8, SerializeResult<O9> o9, SerializeResult<O10> o10, SerializeResult<O11> o11, SerializeResult<O12> o12, SerializeResult<O13> o13, SerializeResult<O14> o14) {
        return anyFail(() -> new Tuples.T15<>(value, o1.value, o2.value, o3.value, o4.value, o5.value, o6.value, o7.value, o8.value, o9.value, o10.value, o11.value, o12.value, o13.value, o14.value), this, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14);
    }

    private <O> SerializeResult<O> anyFail(Supplier<O> success, SerializeResult<?>... other) {
        for(SerializeResult<?> o : other) {
            if(!o.success) return SerializeResult.failure(o.error);
        }
        return SerializeResult.success(success.get());
    }
}
