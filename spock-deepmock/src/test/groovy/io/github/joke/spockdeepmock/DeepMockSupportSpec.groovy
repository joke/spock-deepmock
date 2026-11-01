package io.github.joke.spockdeepmock

import java.lang.reflect.Modifier
import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.IMockObject
import org.spockframework.mock.runtime.MockController
import org.spockframework.runtime.SpecificationContext
import org.spockframework.runtime.extension.IStore
import spock.lang.Specification
import spock.lang.Tag

@Tag('unit')
class DeepMockSupportSpec extends Specification {

    def "options adds the deep mock response to a copy of the caller's options"() {
        def callerOptions = [name: 'person']

        when:
        def options = DeepMockSupport.options(callerOptions)

        then:
        0 * _

        expect:
        options == [name: 'person', defaultResponse: DeepMockSupport.RESPONSE]
        !callerOptions.containsKey('defaultResponse')
    }

    def "options rejects a caller who sets a default response"() {
        when:
        DeepMockSupport.options([defaultResponse: Mock(IDefaultResponse)])

        then:
        def error = thrown(UnsupportedOperationException)
        0 * _

        expect:
        error.message == 'DeepMocks with default response are not supported.'
    }

    def "rejectDefaultResponse always throws"() {
        when:
        DeepMockSupport.rejectDefaultResponse()

        then:
        thrown(UnsupportedOperationException)
        0 * _
    }

    def "arguments wraps a map of options with the deep mock response"() {
        expect:
        DeepMockSupport.arguments([name: 'person']) == [[name: 'person', defaultResponse: DeepMockSupport.RESPONSE]]
    }

    def "arguments places the deep mock response first when no options are given"() {
        expect:
        DeepMockSupport.arguments(String) == [[defaultResponse: DeepMockSupport.RESPONSE], String]
    }

    def "beginWalk returns null outside a specification"() {
        expect:
        DeepMockSupport.beginWalk('not a specification', 1, 2) == null
    }

    def "endWalk returns the target when no walk was begun"() {
        def target = new Object()

        expect:
        DeepMockSupport.endWalk(null, target).is(target)
    }

    def "beginWalk opens a scope and registers a chain walker on the controller"() {
        Specification specification = Mock()
        SpecificationContext context = Mock()
        MockController controller = Mock()

        when:
        def walk = DeepMockSupport.beginWalk(specification, 3, 4)

        then:
        _ * specification.specificationContext >> context
        _ * context.mockController >> controller
        1 * controller.enterScope()
        1 * controller.addInteraction { it instanceof ChainWalker }
        0 * _

        expect:
        walk != null
    }

    def "endWalk leaves the scope and registers one step per deep mock on the path to the target"() {
        Specification specification = Mock()
        SpecificationContext context = Mock()
        MockController controller = Mock()
        IStore store = Mock()
        MockCache cache = Mock()
        def target = new Object()
        def step = DeepMockInteraction.create(invocation('parent'), new Object(), 'parent.child()')

        when:
        def walk = DeepMockSupport.beginWalk(specification, 3, 4)
        def result = DeepMockSupport.endWalk(walk, target)

        then:
        _ * specification.specificationContext >> context
        _ * context.mockController >> controller
        _ * context.getStore { it != null } >> store
        _ * store.getOrComputeIfAbsent(MockCache, { it != null }, MockCache) >> cache
        _ * cache.pathTo(target) >> [step]
        1 * controller.enterScope()
        1 * controller.addInteraction { it instanceof ChainWalker }
        1 * controller.leaveScope()
        1 * controller.addInteraction { verifyAll(it) { line == 3; column == 4 } }
        0 * _

        expect:
        result.is(target)
    }

    def "the utility class has a private constructor"() {
        def constructor = DeepMockSupport.declaredConstructors[0]
        constructor.accessible = true

        expect:
        Modifier.isPrivate(constructor.modifiers)
        constructor.newInstance() instanceof DeepMockSupport
    }

    def "defaultResponseRejected describes the rejected option"() {
        expect:
        DeepMockSupport.defaultResponseRejected().message == 'DeepMocks with default response are not supported.'
    }

    private IMockInvocation invocation(String target) {
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()
        invocation.mockObject >> mockObject
        mockObject.instance >> target
        invocation.method >> Sample.method('primitive')
        invocation.arguments >> []
        invocation
    }
}
