package io.github.joke.test


import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class Nested {
    String name
    Nested child
}
