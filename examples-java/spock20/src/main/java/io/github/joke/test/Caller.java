package io.github.joke.test;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Caller {
    private final Nested nested;

    public String call() {
        return nested.getChild().getChild().getName();
    }
}
