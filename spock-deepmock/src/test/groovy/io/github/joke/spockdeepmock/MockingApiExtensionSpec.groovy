package io.github.joke.spockdeepmock

import org.spockframework.runtime.InvalidSpecException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Tag
import spock.mock.MockingApi

@Tag('unit')
class MockingApiExtensionSpec extends Specification {

    static Closure block = { }

    @Shared
    MockingApi api = Mock()

    def "every overload rejects a call that the compiler did not rewrite"() {
        when:
        attempt()

        then:
        thrown(InvalidSpecException)
        0 * _

        where:
        attempt << [
            { MockingApiExtension.DeepMock(api) },
            { MockingApiExtension.DeepMock(api, [name: 'x']) },
            { MockingApiExtension.DeepMock(api, String) },
            { MockingApiExtension.DeepMock(api, [name: 'x'], String) },
            { MockingApiExtension.DeepMock(api, block) },
            { MockingApiExtension.DeepMock(api, [name: 'x'], block) },
            { MockingApiExtension.DeepMock(api, String, block) },
            { MockingApiExtension.DeepMock(api, [name: 'x'], String, block) },
            { MockingApiExtension.GroovyDeepMock(api) },
            { MockingApiExtension.GroovyDeepMock(api, [name: 'x']) },
            { MockingApiExtension.GroovyDeepMock(api, String) },
            { MockingApiExtension.GroovyDeepMock(api, [name: 'x'], String) },
            { MockingApiExtension.GroovyDeepMock(api, block) },
            { MockingApiExtension.GroovyDeepMock(api, [name: 'x'], block) },
            { MockingApiExtension.GroovyDeepMock(api, String, block) },
            { MockingApiExtension.GroovyDeepMock(api, [name: 'x'], String, block) },
        ]
    }

    def "the utility class cannot be instantiated"() {
        def constructor = MockingApiExtension.declaredConstructors[0]
        constructor.accessible = true

        when:
        constructor.newInstance()

        then:
        def e = thrown(java.lang.reflect.InvocationTargetException)
        0 * _

        expect:
        e.cause instanceof UnsupportedOperationException
    }

    def "the rejection explains that the compiler did not rewrite the call"() {
        expect:
        MockingApiExtension.invalidMockCreation().message.contains('The compiler did not rewrite this call')
    }
}
