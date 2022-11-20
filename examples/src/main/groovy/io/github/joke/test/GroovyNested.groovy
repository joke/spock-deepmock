package io.github.joke.test


import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class GroovyNested {
    String name
    GroovyNested child

    GroovyNested child(final String name) {
        child
    }
}
