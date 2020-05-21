package de.xckk.test.deepmock;

import lombok.Data;

@Data
public class Nested {
    private final String name;
    private final Nested child;
}
