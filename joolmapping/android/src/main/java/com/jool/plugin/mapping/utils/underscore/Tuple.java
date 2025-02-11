package com.jool.plugin.mapping.utils.underscore;

public class Tuple<A, B> {
    private final A first;
    private final B second;

    public Tuple(final A first, final B second) {
        super();
        this.first = first;
        this.second = second;
    }

    public static <A, B> Tuple<A, B> create(final A a, final B b) {
        return new Tuple<>(a, b);
    }

    public A fst() {
        return first;
    }

    public B snd() {
        return second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
