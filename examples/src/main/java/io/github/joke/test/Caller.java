package io.github.joke.test;

public class Caller {
    private final Nested nested;

    public Caller(final Nested nested) {
        this.nested = nested;
    }

    public String call() {
        return nested.getChild().getChild().getName();
    }

    public String callWithParameters() {
        return nested.child("first").child("second").getName();
    }
}
