package io.github.joke.test

import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.WrongInvocationOrderError
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

class ChainInteractionTest extends Specification {

    Nested nested = DeepMock()

    def 'verified chain with catch-all'() {
        when:
        def res = new Caller(nested).call()

        then:
        1 * nested.child.child.name >> 'Hello'
        0 * _
        res == 'Hello'
    }

    def 'verified chain with arguments and catch-all'() {
        when:
        def res = new Caller(nested).callWithParameters()

        then:
        1 * nested.child('first').child('second').name >> 'Hello'
        0 * _
        res == 'Hello'
    }

    def 'saved nested mock with catch-all'() {
        given:
        def second = nested.child

        when:
        def res = new Caller(nested).call()

        then:
        1 * second.child.name >> 'Hello'
        0 * _
        res == 'Hello'
    }

    def 'catch-all declared before the chain'() {
        when:
        def res = new Caller(nested).call()

        then:
        0 * _
        1 * nested.child.child.name >> 'Hello'
        res == 'Hello'
    }

    def 'step verification declared after the chain fails'() {
        def source = '''
            import spock.lang.Specification

            class StepAfterChainSpec extends Specification {
                io.github.joke.test.Nested nested = DeepMock()

                def 'step after chain'() {
                    when:
                    nested.child.name

                    then:
                    1 * nested.child.name >> 'Hello'
                    1 * nested.child
                }
            }
        '''

        when:
        new EmbeddedSpecRunner().run(source)

        then:
        def error = thrown(TooFewInvocationsError)

        expect:
        error.message.contains('1 * nested.child   (0 invocations)')
    }

    def 'step verification declared before the chain passes'() {
        when:
        def res = nested.child.name

        then:
        1 * nested.child
        1 * nested.child.name >> 'Hello'
        res == 'Hello'
    }

    def 'unstubbed chain with ordered then blocks passes'() {
        given:
        Runnable other = Mock()
        Runnable another = Mock()

        when:
        nested.child
        nested.child
        other.run()
        another.run()

        then:
        1 * other.run()

        then:
        1 * another.run()
    }

    def 'unstubbed chain in a cleanup block returns a nested mock'() {
        expect:
        true

        cleanup:
        assert nested.child.child != null
    }

    def 'implicit step interaction shows the call and the declaring line'() {
        def source = '''
            import spock.lang.Specification

            class ReadableStepSpec extends Specification {
                io.github.joke.test.Nested nested = DeepMock()
                Runnable other = Mock()

                def 'readable step'() {
                    when:
                    nested.child.name
                    other.run()
                    nested.child

                    then:
                    1 * nested.child.name >> 'Hello'

                    then:
                    1 * other.run()
                }
            }
        '''
        // EmbeddedSpecCompiler compiles source.trim(), so count the lines of the trimmed source
        def declaringLine = source.trim().readLines().findIndexOf { it.contains("1 * nested.child.name >> 'Hello'") } + 1

        when:
        new EmbeddedSpecRunner().run(source)

        then:
        def error = thrown(WrongInvocationOrderError)

        expect:
        error.message.contains('nested.getChild()')
        error.message.contains("line ${declaringLine}")
    }
}
