package io.github.joke.test

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
        0 * _

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
        0 * _

        expect:
        res == 'Hello'
    }

    def 'concurrent mock'() {
        setup:
        Nested nested1 = DeepMock()
        Nested nested2 = DeepMock()

        when:
        def res1 = new Caller(nested1).call()
        def res2 = new Caller(nested2).call()

        then:
        1 * nested2.child.child.name >> 'World'
        1 * nested1.child.child.name >> 'Hello'
        0 * _

        expect:
        res1 == 'Hello'
        res2 == 'World'
    }

    def 'calling nested mock'() {
        setup:
        def caller = new Caller(first)

        when:
        def res = caller.call()

        then:
        1 * first.child.child.name >> 'Hello'
        0 * _

        expect:
        res == 'Hello'
    }

    def 'calling nested parameter mock'() {
        setup:
        def caller = new Caller(first)

        when:
        def res = caller.callWithParameters()

        then:
        1 * first.child('first').child('second').name >> 'Hello'
        0 * _

        expect:
        res == 'Hello'
    }
}
