package io.github.joke.test

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class Caller {
    Nested nested

    def call() {
        nested.child.child.name
    }
}
