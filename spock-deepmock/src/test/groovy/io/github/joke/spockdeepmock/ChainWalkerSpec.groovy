package io.github.joke.spockdeepmock

import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.IMockObject
import org.spockframework.mock.ZeroOrNullResponse
import spock.lang.Specification
import spock.lang.Tag

@Tag('unit')
class ChainWalkerSpec extends Specification {

    def "matches a call on a deep mock made by the thread that created the walker"() {
        def walker = new ChainWalker()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def matched = walker.matches(invocation)

        then:
        _ * invocation.mockObject >> mockObject
        _ * mockObject.defaultResponse >> DeepMockSupport.RESPONSE
        0 * _

        expect:
        matched
    }

    def "does not match a call made by another thread"() {
        def walker = new ChainWalker()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()
        def matches = []

        when:
        Thread.start { matches << walker.matches(invocation) }.join()

        then:
        _ * invocation.mockObject >> mockObject
        _ * mockObject.defaultResponse >> DeepMockSupport.RESPONSE
        0 * _

        expect:
        matches == [false]
    }

    def "does not match a call on a mock without a deep mock response"() {
        def walker = new ChainWalker()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def matched = walker.matches(invocation)

        then:
        _ * invocation.mockObject >> mockObject
        _ * mockObject.defaultResponse >> ZeroOrNullResponse.INSTANCE
        0 * _

        expect:
        !matched
    }

    def "accept answers the call with the deep mock response"() {
        def walker = new ChainWalker()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = walker.accept(invocation).get()

        then:
        _ * invocation.mockObject >> mockObject
        _ * invocation.method >> Sample.method('primitive')
        _ * invocation.arguments >> []
        0 * _

        expect:
        answer == 0
    }

    def "reports the walk as line and column minus one, never counted"() {
        def walker = new ChainWalker()

        expect:
        walker.line == -1
        walker.column == -1
        walker.text == 'deep mock walk'
        walker.acceptedInvocations.empty
        walker.computeSimilarityScore(Mock(IMockInvocation)) == 0
        walker.describeMismatch(Mock(IMockInvocation)) == ''
        walker.satisfied
        !walker.exhausted
        !walker.required
    }
}
