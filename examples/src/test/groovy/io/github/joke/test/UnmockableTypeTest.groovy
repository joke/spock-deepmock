package io.github.joke.test

import org.spockframework.runtime.InvalidSpecException
import spock.lang.Specification

class UnmockableTypeTest extends Specification {

    Nested nested = DeepMock()

    def 'unstubbed call returning a final type fails with a message that names the call'() {
        when:
        def name = nested.name

        then:
        def e = thrown(InvalidSpecException)
        e.message == "Cannot return a deep mock from nested.getName(): java.lang.String is final and cannot be mocked. " +
                "Stub the call instead, for example: nested.getName() >> <value>"
    }

    def 'stubbed call returning a final type works'() {
        given:
        nested.name >> 'first'

        expect:
        nested.name == 'first'
    }
}
