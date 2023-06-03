package de.kehrweek.multipartlib.api.util;

import java.util.function.Function;

/**
 * Everyone gets to implement their own Pair class, so why not me too.
 */
public record Pair<F, S>(F first, S second) {

    public Pair<F, S> first(F first) {
        return new Pair<>(first, second());
    }

    public Pair<F, S> second(S second) {
        return new Pair<>(first(), second);
    }

    public <A, B> Pair<A, B> map(Function<F, A> firstFunction, Function<S, B> secondFunction) {
        return new Pair<>(firstFunction.apply(first()), secondFunction.apply(second()));
    }

    public <T> Pair<T, S> mapFirst(Function<F, T> function) {
        return new Pair<>(function.apply(first()), second());
    }

    public <T> Pair<F, T> mapSecond(Function<S, T> function) {
        return new Pair<>(first(), function.apply(second()));
    }

    public F a() {
        return first();
    }

    public S b() {
        return second();
    }

    public F left() {
        return first();
    }

    public S right() {
        return second();
    }

    public F key() {
        return first();
    }

    public S value() {
        return second();
    }

}
