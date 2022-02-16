package io.github.joke.test;

import lombok.Data;

@Data
public class Nested {
    private final String name;
    private final Nested child;
}
