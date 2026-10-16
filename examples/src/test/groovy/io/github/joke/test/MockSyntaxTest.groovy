package io.github.joke.test

import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.mock.MockMakers

class MockSyntaxTest extends Specification {

    def mockUtil = new MockUtil()

    def 'mock maker option names the mock maker'() {
        when:
        Nested nested = DeepMock(mockMaker: MockMakers.byteBuddy)

        then:
        mockUtil.asMock(nested).configuration.mockMaker.mockMakerId == MockMakers.byteBuddy.mockMakerId
    }

    def 'interaction closure with a chain'() {
        Nested nested = DeepMock {
            it.child.name >> 'Hello'
        }

        expect:
        nested.child.name == 'Hello'
    }

    def 'type from a variable'() {
        given:
        Class type = Nested

        when:
        def nested = DeepMock(type)

        then:
        nested instanceof Nested
    }

    def 'options and type from variables'() {
        given:
        def options = [name: 'customName']
        Class type = Nested

        when:
        def nested = DeepMock(options, type)

        then:
        nested instanceof Nested
        mockUtil.asMock(nested).name == 'customName'
    }
}
