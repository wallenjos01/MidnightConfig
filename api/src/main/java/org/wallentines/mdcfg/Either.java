package org.wallentines.mdcfg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Either<L, R> {

    private final L left;
    private final R right;

    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(@NotNull L left) {
        return new Either<L, R>(left, null);
    }

    public static <L, R> Either<L, R> right(@NotNull R right) {
        return new Either<L, R>(null, right);
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasRight() {
        return right != null;
    }

    @Nullable
    public L left() {
        return left;
    }

    @Nullable
    public R right() {
        return right;
    }


    public L leftOrThrow() {
        if(!hasLeft()) throw new IllegalStateException("No left");
        return left;
    }

    public R rightOrThrow() {
        if(!hasRight()) throw new IllegalStateException("No right");
        return right;
    }


    public L leftOr(L other) {
        return hasLeft() ? left : other;
    }

    public R rightOr(R other) {
        return hasRight() ? right : other;
    }


    public L leftOrGet(Supplier<L> getter) {
        return hasLeft() ? left : getter.get();
    }

    public R rightOrGet(Supplier<R> getter) {
        return hasRight() ? right : getter.get();
    }
}
