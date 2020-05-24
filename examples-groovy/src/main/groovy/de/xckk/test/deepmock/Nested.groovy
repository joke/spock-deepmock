package de.xckk.test.deepmock


import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class Nested {
    String name
    Nested child
}
