package io.github.joke.test

import org.spockframework.mock.MockUtil
import spock.lang.Specification

class FallbackAnswerTest extends Specification {

    def mockUtil = new MockUtil()

    Nested nested = DeepMock()
    GroovyNested groovyNested = GroovyDeepMock()

    def 'unstubbed void call returns normally'() {
        when:
        nested.close()
        groovyNested.close()

        then:
        noExceptionThrown()
    }

    def 'unstubbed calls with primitive return types return the default value'() {
        expect:
        nested.size() == 0
        nested.isEnabled() == false
        groovyNested.size() == 0
        groovyNested.isEnabled() == false
    }

    def 'unstubbed calls with reference types that cannot be mocked return null'() {
        expect:
        nested.getName() == null
        nested.code() == null
        groovyNested.getName() == null
        groovyNested.code() == null
    }

    def 'unstubbed call returning a final class that a mock maker can mock returns a deep mock'() {
        expect:
        nested.leaf() instanceof FinalLeaf
        mockUtil.isMock(nested.leaf())
        groovyNested.leaf() instanceof GroovyFinalLeaf
        // Spock's isMock is false for Groovy mocks of final classes, so check that the call is intercepted:
        // an unstubbed value() returns null, while the real GroovyFinalLeaf would return 'leaf'.
        groovyNested.leaf().value() == null
    }

    def 'loop over an unstubbed list runs zero times'() {
        when:
        def count = 0
        for (child in nested.children()) {
            count++
        }

        then:
        count == 0
    }

    def 'repeated unstubbed calls on a type that cannot be mocked keep returning null'() {
        when:
        def names = (1..3).collect { nested.getName() }

        then:
        names == [null, null, null]
    }

    def 'stubbed call at the end of a chain returns the stub'() {
        setup:
        nested.child.child.name >> 'third'

        expect:
        nested.child.child.name == 'third'
    }
}
