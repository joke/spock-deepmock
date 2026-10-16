package io.github.joke.test

import groovy.transform.CompileStatic
import spock.lang.Specification

class NestedIdentityTest extends Specification {

    Nested nested = DeepMock()

    def 'repeated property access returns the same nested mock'() {
        expect:
        nested.child.is(nested.child)
    }

    def 'saved nested mock is returned again'() {
        def second = nested.child

        expect:
        nested.child.is(second)
    }

    def 'different methods with the same return type return different nested mocks'() {
        expect:
        !nested.child.is(nested.child('x'))
        !nested.child.is(nested.sibling())
    }

    def 'different arguments return different nested mocks'() {
        expect:
        !nested.child('first').is(nested.child('second'))
    }

    def 'equal arguments return the same nested mock'() {
        expect:
        nested.child('first').is(nested.child('first'))
    }

    def 'different methods keep their nested mocks across interaction scopes'() {
        when:
        def child = nested.child

        then:
        !child.is(nested.child('x'))
    }

    def 'overloads with equal arguments return different nested mocks'() {
        expect:
        !nested.lookup('x').is(lookupAsObject(nested, 'x'))
        nested.lookup('x') instanceof Nested
    }

    def 'overloads with equal arguments keep their nested mocks across interaction scopes'() {
        when:
        def stringOverload = nested.lookup('x')

        then:
        !stringOverload.is(lookupAsObject(nested, 'x'))
    }

    @CompileStatic
    static Object lookupAsObject(final Nested nested, final Object key) {
        nested.lookup(key)
    }
}
