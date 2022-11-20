package io.github.joke.test;

public class Nested {
    private final String name;
    private final Nested child;

    public Nested(final String name, final Nested child) {
        this.name = name;
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public Nested getChild() {
        return child;
    }

    public Nested child(final String name) {
        return child;
    }

}
