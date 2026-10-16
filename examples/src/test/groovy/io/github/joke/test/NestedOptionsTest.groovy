package io.github.joke.test

import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.mock.MockMakers

class NestedOptionsTest extends Specification {

    def mockUtil = new MockUtil()

    def 'nested mock uses the mock maker of the parent'() {
        given:
        Nested nested = DeepMock(mockMaker: MockMakers.byteBuddy)

        expect:
        nested.leaf() == null
    }

    def 'nested mock is not verified when the parent is not verified'() {
        given:
        Nested nested = DeepMock(verified: false)

        expect:
        !mockUtil.asMock(nested.child).configuration.verified
    }

    def 'nested mock does not use Objenesis when the parent does not'() {
        given:
        Nested nested = DeepMock(useObjenesis: false)

        expect:
        !mockUtil.asMock(nested.children()).configuration.useObjenesis
    }

    def 'additional interfaces stay on the parent'() {
        given:
        Nested nested = DeepMock(additionalInterfaces: [Runnable])

        expect:
        nested instanceof Runnable
        !(nested.child instanceof Runnable)
    }
}
