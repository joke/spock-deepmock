package io.github.joke.test;

import java.util.ArrayList;
import java.util.List;

public class Nested {
    private final String name;
    private final Nested child;

    public Nested() {
        this(null, null);
    }

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

    public void close() {
    }

    public int size() {
        return 0;
    }

    public boolean isEnabled() {
        return false;
    }

    public Integer code() {
        return 0;
    }

    public List<Nested> children() {
        return new ArrayList<>();
    }

    public FinalLeaf leaf() {
        return new FinalLeaf();
    }

}
