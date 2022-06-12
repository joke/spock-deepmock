package io.github.joke.test

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class GroovyCaller {
    GroovyNested nested

    def call() {
        nested.child.child.name
    }
}
