package io.github.joke.test

import org.spockframework.mock.MockUtil
import spock.lang.Specification

class GroovyMockTest extends Specification {

    def mockUtil = new MockUtil()

    GroovyNested first = GroovyDeepMock()

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
        def caller = new GroovyCaller(first)

        when:
        def res = caller()

        then:
        1 * first.child.child.name >> 'Hello'
        0 * _

        expect:
        res == 'Hello'
    }

    def 'saving mocks in variables'() {
        setup:
        def caller = new GroovyCaller(first)
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
        GroovyNested namedMock = GroovyDeepMock(name: 'customName')
        def caller = new GroovyCaller(namedMock)

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
        GroovyNested nested1 = GroovyDeepMock()
        GroovyNested nested2 = GroovyDeepMock()

        when:
        def res1 = new GroovyCaller(nested1).call()
        def res2 = new GroovyCaller(nested2).call()

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
        def caller = new GroovyCaller(first)

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
        def caller = new GroovyCaller(first)

        when:
        def res = caller.callWithParameters()

        then:
        1 * first.child('first').child('second').name >> 'Hello'
        0 * _

        expect:
        res == 'Hello'
    }
}
