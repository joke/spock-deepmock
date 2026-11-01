package io.github.joke.spockdeepmock

import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.IMockMethod
import org.spockframework.mock.IMockObject
import org.spockframework.runtime.SpecificationContext
import org.spockframework.runtime.extension.IStore
import spock.lang.Specification
import spock.lang.Tag

@Tag('unit')
class DeepMockResponseSpec extends Specification {

    def response = new DeepMockResponse()

    def "primitive and array return types are never mockable"() {
        IMockInvocation invocation = Mock()

        when:
        def mockable = response.isNeverMockable(invocation)

        then:
        _ * invocation.method >> Sample.method(methodName)
        0 * _

        expect:
        mockable == expected

        where:
        methodName  | expected
        'primitive' | true
        'array'     | true
        'object'    | false
    }

    def "a call with a primitive return type is answered with the zero value"() {
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * invocation.method >> Sample.method('primitive')
        _ * invocation.mockObject >> mockObject
        0 * _

        expect:
        answer == 0
    }

    def "a call with an array return type is answered with null"() {
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * invocation.method >> Sample.method('array')
        _ * invocation.mockObject >> mockObject
        0 * _

        expect:
        answer == null
    }

    def "the fallback answers a reference return type with null"() {
        IMockInvocation invocation = Mock()

        when:
        def answer = response.fallback(invocation)

        then:
        _ * invocation.method >> Sample.method('object')
        0 * _

        expect:
        answer == null
    }

    def "a call with a reference return type is answered with the deep mock cached for the call"() {
        def cached = new Object()
        MockCache cache = Mock()
        Specification specification = Mock()
        SpecificationContext context = Mock()
        IStore store = Mock()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * specification.specificationContext >> context
        _ * context.getStore { it != null } >> store
        _ * store.getOrComputeIfAbsent(MockCache, { it != null }, MockCache) >> cache
        _ * invocation.mockObject >> mockObject
        _ * invocation.method >> Sample.method('object')
        _ * invocation.arguments >> []
        _ * mockObject.specification >> specification
        _ * mockObject.instance >> new Object()
        1 * cache.findOrCreate(invocation) >> Optional.of(DeepMockInteraction.create(invocation, cached, 'p'))
        0 * _

        expect:
        answer.is(cached)
    }

    def "a call with a reference return type without a cached deep mock is answered with null"() {
        MockCache cache = Mock()
        Specification specification = Mock()
        SpecificationContext context = Mock()
        IStore store = Mock()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * specification.specificationContext >> context
        _ * context.getStore { it != null } >> store
        _ * store.getOrComputeIfAbsent(MockCache, { it != null }, MockCache) >> cache
        _ * invocation.mockObject >> mockObject
        _ * invocation.method >> Sample.method('object')
        _ * invocation.arguments >> []
        _ * mockObject.specification >> specification
        _ * mockObject.instance >> new Object()
        1 * cache.findOrCreate(invocation) >> Optional.empty()
        0 * _

        expect:
        answer == null
    }

    def "a call without a cached deep mock is answered by the fallback"() {
        DeepMockResponse response = Spy()
        MockCache cache = Mock()
        Specification specification = Mock()
        SpecificationContext context = Mock()
        IStore store = Mock()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * specification.specificationContext >> context
        _ * context.getStore { it != null } >> store
        _ * store.getOrComputeIfAbsent(MockCache, { it != null }, MockCache) >> cache
        _ * invocation.mockObject >> mockObject
        _ * invocation.method >> Sample.method('object')
        _ * invocation.arguments >> []
        _ * mockObject.specification >> specification
        _ * mockObject.instance >> new Object()
        1 * cache.findOrCreate(invocation) >> Optional.empty()
        1 * response.fallback(invocation) >> 'fallback'
        _ * response._
        0 * _

        expect:
        answer == 'fallback'
    }

    def "equals on a deep mock is answered by the java.lang.Object interactions of the mock"() {
        def instance = new Object()
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * invocation.mockObject >> mockObject
        _ * mockObject.instance >> instance
        _ * invocation.method >> methodStub('equals', [Object], boolean)
        _ * invocation.arguments >> [instance]
        0 * _

        expect:
        answer == true
    }

    def "toString on a deep mock is named by the java.lang.Object interactions of the mock"() {
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()

        when:
        def answer = response.respond(invocation)

        then:
        _ * invocation.mockObject >> mockObject
        _ * mockObject.name >> 'person'
        _ * mockObject.type >> Sample
        _ * invocation.method >> methodStub('toString', [], String)
        _ * invocation.arguments >> []
        0 * _

        expect:
        answer == "Mock for type 'Sample' named 'person'"
    }

    private IMockMethod methodStub(String name, List<Class<?>> parameterTypes, Class<?> returnType) {
        IMockMethod method = Mock()
        method.name >> name
        method.parameterTypes >> parameterTypes
        method.exactParameterTypes >> parameterTypes
        method.returnType >> returnType
        method.exactReturnType >> returnType
        method
    }
}
