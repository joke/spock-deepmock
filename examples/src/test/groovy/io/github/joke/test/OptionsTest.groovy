package io.github.joke.test

import org.spockframework.mock.ZeroOrNullResponse
import spock.lang.Specification

class OptionsTest extends Specification {

    def 'default response option is rejected'() {
        when:
        Nested nested = DeepMock(defaultResponse: ZeroOrNullResponse.INSTANCE)

        then:
        thrown(UnsupportedOperationException)
    }

    def 'caller options are not modified'() {
        def options = [name: 'customName']

        when:
        Nested nested = DeepMock(options)

        then:
        options.keySet() == ['name'] as Set
    }
}
