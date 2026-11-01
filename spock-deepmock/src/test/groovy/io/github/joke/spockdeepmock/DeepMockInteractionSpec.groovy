package io.github.joke.spockdeepmock

import org.spockframework.mock.IMockInteraction
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.IMockMethod
import org.spockframework.mock.IMockObject
import spock.lang.Specification
import spock.lang.Tag

@Tag('unit')
class DeepMockInteractionSpec extends Specification {

    def target = new Object()
    def mock = new Object()

    def "the step returns the mock and remembers the target as its parent"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'primitive', []), mock, 'root.primitive()')

        when:
        def supplied = interaction.accept(invocation(target, 'primitive', [])).get()

        then:
        0 * _

        expect:
        interaction.parent.is(target)
        supplied.is(mock)
    }

    def "a new step has no position and describes its path"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'primitive', []), mock, 'root.primitive()')

        expect:
        interaction.line == -1
        interaction.column == -1
        interaction.text == '_ * root.primitive() (deep mock step of line -1)'
        interaction.toString() == interaction.text
    }

    def "stepAt returns a copy at the given position that keeps the mock and the parent"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'primitive', []), mock, 'root.primitive()')

        when:
        def step = interaction.stepAt(7, 9)
        def supplied = step.accept(null).get()

        then:
        0 * _

        expect:
        step.line == 7
        step.column == 9
        step.text == '_ * root.primitive() (deep mock step of line 7)'
        step.parent.is(target)
        supplied.is(mock)
        !step.is(interaction)
    }

    def "matches a call with the same target, method and arguments"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'withArguments', ['a', 1]), mock, 'p')

        expect:
        interaction.matches(invocation(target, 'withArguments', ['a', 1]))
    }

    def "does not match a call with other arguments"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'withArguments', ['a', 1]), mock, 'p')

        expect:
        !interaction.matches(invocation(target, 'withArguments', ['b', 1]))
    }

    def "does not match a call on another target"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'primitive', []), mock, 'p')

        expect:
        !interaction.matches(invocation(new Object(), 'primitive', []))
    }

    def "does not match a call to another method"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'primitive', []), mock, 'p')

        expect:
        !interaction.matches(invocation(target, 'array', []))
    }

    def "similarity is zero for a match and weighs each mismatched constraint by its position"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'withArguments', ['a', 1]), mock, 'p')

        expect:
        interaction.computeSimilarityScore(invocation(target, 'withArguments', ['a', 1])) == 0
        interaction.computeSimilarityScore(invocation(target, 'withArguments', ['b', 1])) == 1
        interaction.computeSimilarityScore(invocation(new Object(), 'withArguments', ['a', 1])) == 4
    }

    def "a comparison that throws propagates out of the similarity score"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'withArguments', ['a', 1]), mock, 'p')

        when:
        def current = invocation(target, 'withArguments', [new Unequal(), 1])
        interaction.computeSimilarityScore(current)

        then:
        thrown(IllegalStateException)
        0 * _
    }

    def "a mismatch describes nothing for a match and something for a mismatch"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'withArguments', ['a', 1]), mock, 'p')

        expect:
        interaction.describeMismatch(invocation(target, 'withArguments', ['a', 1])) == ''
        !interaction.describeMismatch(invocation(target, 'withArguments', ['b', 1])).empty
    }

    def "a deep mock step is always satisfied, never exhausted or required, and accepts no invocations"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'primitive', []), mock, 'p')

        expect:
        interaction.satisfied
        !interaction.exhausted
        !interaction.required
        interaction.acceptedInvocations.empty
    }

    def "a call with other parameter types does not match and is described by them"() {
        def interaction = DeepMockInteraction.create(invocation(target, 'withArguments', ['a', 1]), mock, 'p')
        IMockMethod method = Mock()

        when:
        def other = invocation(target, method, ['a', 1])
        def matched = interaction.matches(other)
        def description = interaction.describeMismatch(other)

        then:
        _ * method.name >> 'withArguments'
        _ * method.parameterTypes >> [Object, int]
        _ * method._
        0 * _

        expect:
        !matched
        description == 'parameter types [class java.lang.String, int] do not match [class java.lang.Object, int]'
    }

    private IMockInvocation invocation(Object instance, String methodName, List<Object> arguments) {
        invocation(instance, Sample.method(methodName), arguments)
    }

    private IMockInvocation invocation(Object instance, IMockMethod method, List<Object> arguments) {
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()
        invocation.mockObject >> mockObject
        mockObject.instance >> instance
        mockObject.matches({ it.is(instance) }) { it == null || it instanceof IMockInteraction } >> true
        invocation.method >> method
        invocation.arguments >> arguments
        invocation
    }

    static class Unequal {

        @Override
        boolean equals(final Object other) {
            throw new IllegalStateException('boom')
        }

        @Override
        int hashCode() {
            0
        }
    }
}
