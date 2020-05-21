package de.xckk.test.deepmock

import org.spockframework.mock.MockUtil
import spock.lang.Specification

class MockTest extends Specification {

    def mockUtil = new MockUtil()

    Nested first = DeepMock()

    def 'check deep mock is returned'() {
        setup:
        first.name >> 'first'
        first.child.name >> 'second'
        first.child.child.name >> 'third'

        expect:
        mockUtil.isMock first
        mockUtil.isMock first.child
        mockUtil.isMock first.child.child

        first.name == 'first'
        first.child.name == 'second'
        first.child.child.name == 'third'
    }

    def 'calling nested mock'() {
        setup:
        def caller = new Caller(first)

        when:
        def res = caller.call()

        then:
        1 * first.child.child.name >> 'Hello'

        expect:
        res == 'Hello'
    }

    def 'saving mocks in variables'() {
        setup:
        def caller = new Caller(first)
        def second = first.child

        when:
        def res = caller.call()

        then:
        1 * second.child.name >> 'Hello'

        expect:
        res == 'Hello'
        first.child == second
    }

    def 'naming mock'() {
        setup:
        Nested namedMock = DeepMock(name: 'customName')
        def caller = new Caller(namedMock)

        when:
        def res = caller.call()

        then:
        1 * namedMock.child.child.name >> 'Hello'

        expect:
        res == 'Hello'
    }
}
