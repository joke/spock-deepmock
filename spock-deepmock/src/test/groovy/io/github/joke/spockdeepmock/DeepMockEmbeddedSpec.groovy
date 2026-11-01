package io.github.joke.spockdeepmock

import spock.lang.Specification
import spock.lang.Tag
import spock.util.EmbeddedSpecRunner

/**
 * Runs deep mock specifications in-process, so the paths that create real Spock mocks are covered.
 */
@Tag('unit')
class DeepMockEmbeddedSpec extends Specification {

    def "a chain of Java mocks is stubbed at its end"() {
        when:
        new EmbeddedSpecRunner().run('''
            import spock.lang.Specification

            class JavaChainSpec extends Specification {
                Holder holder = DeepMock()

                def "chain"() {
                    when:
                    def name = holder.child().name()

                    then:
                    1 * holder.child().name() >> 'Hello'
                    0 * _
                    name == 'Hello'
                }
            }

            interface Holder { Child child() }
            interface Child { String name() }
        ''')

        then:
        noExceptionThrown()
        0 * _
    }

    def "a chain through a Groovy object is stubbed at its end"() {
        when:
        new EmbeddedSpecRunner().run('''
            import spock.lang.Specification

            class GroovyChainSpec extends Specification {
                Holder holder = DeepMock()

                def "chain"() {
                    when:
                    def name = holder.child().name()

                    then:
                    1 * holder.child().name() >> 'Hello'
                    0 * _
                    name == 'Hello'
                }
            }

            interface Holder { Child child() }
            interface Child extends groovy.lang.GroovyObject { String name() }
        ''')

        then:
        noExceptionThrown()
        0 * _
    }

    def "a primitive at the end of a chain is answered with its zero value"() {
        when:
        new EmbeddedSpecRunner().run('''
            import spock.lang.Specification

            class PrimitiveSpec extends Specification {
                Holder holder = DeepMock()

                def "primitive"() {
                    expect:
                    holder.child().size() == 0
                }
            }

            interface Holder { Child child() }
            interface Child { int size() }
        ''')

        then:
        noExceptionThrown()
        0 * _
    }

    def "a mock maker given to the root mock is used by the deep mocks below it"() {
        when:
        new EmbeddedSpecRunner().run('''
            import spock.lang.Specification

            class MockMakerSpec extends Specification {
                Holder holder = DeepMock(mockMaker: spock.mock.MockMakers.mockito)

                def "chain"() {
                    when:
                    def name = holder.child().name()

                    then:
                    1 * holder.child().name() >> 'Hello'
                    0 * _
                    name == 'Hello'
                }
            }

            interface Holder { Child child() }
            interface Child { String name() }
        ''')

        then:
        noExceptionThrown()
        0 * _
    }

    def "a root mock with no stubbed call answers its chain with null"() {
        when:
        new EmbeddedSpecRunner().run('''
            import spock.lang.Specification

            class NullChainSpec extends Specification {
                Holder holder = DeepMock()

                def "chain"() {
                    expect:
                    holder.child().name() == null
                }
            }

            interface Holder { Child child() }
            interface Child { String name() }
        ''')

        then:
        noExceptionThrown()
        0 * _
    }
}
