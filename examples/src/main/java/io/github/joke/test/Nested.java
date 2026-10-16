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

    public Nested sibling() {
        return child;
    }

    public Nested lookup(final String key) {
        return child;
    }

    public Object lookup(final Object key) {
        return child;
    }

}
