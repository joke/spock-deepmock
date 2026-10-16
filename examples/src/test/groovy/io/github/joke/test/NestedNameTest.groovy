package io.github.joke.test

import org.spockframework.mock.MockUtil
import spock.lang.Specification

class NestedNameTest extends Specification {

    def mockUtil = new MockUtil()

    Nested nested = DeepMock()

    def 'property chain names the nested mock after the getter calls'() {
        expect:
        mockUtil.asMock(nested.child.child).name == 'nested.getChild().getChild()'
    }

    def 'method call names the nested mock after its arguments'() {
        expect:
        mockUtil.asMock(nested.child('first')).name == "nested.child('first')"
    }

    def 'mock argument names the nested mock by the name of the argument mock'() {
        given:
        Nested key = DeepMock(name: 'key')

        expect:
        mockUtil.asMock(nested.lookup(key)).name == 'nested.lookup(key)'
    }

    def 'parent without a name uses the simple name of its type'() {
        given:
        Nested parent = DeepMock(name: null)

        expect:
        mockUtil.asMock(parent.child).name == 'Nested.getChild()'
    }
}
