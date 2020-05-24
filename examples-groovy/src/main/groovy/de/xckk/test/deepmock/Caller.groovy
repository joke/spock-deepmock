package de.xckk.test.deepmock

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
