package io.github.joke.spockdeepmock

import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.IMockConfiguration
import org.spockframework.mock.IMockInteraction
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.IMockObject
import org.spockframework.mock.MockImplementation
import spock.lang.Specification
import spock.lang.Tag
import spock.mock.IMockMakerSettings

interface GroovyNamed extends GroovyObject {
    String name()
}

class FailingCache extends MockCache {

    int attempts = 0

    FailingCache() {
        super(DeepMockSupport.RESPONSE)
    }

    @Override
    Object newMock(
            Specification specification,
            Type exactReturnType,
            MockImplementation implementation,
            Map<String, Object> options,
            String name) {
        attempts++
        throw new CannotCreateMockException(int.class, 'no mock maker')
    }
}

@Tag('unit')
class MockCacheSpec extends Specification {

    MockCache cache = Spy(constructorArgs: [DeepMockSupport.RESPONSE])

    def "a method returning a Groovy object is implemented by a Groovy mock, any other by a Java mock"() {
        expect:
        cache.implementationOf(invocation(new Object(), 'root', 'groovyObject', [])) == MockImplementation.GROOVY
        cache.implementationOf(invocation(new Object(), 'root', 'primitive', [])) == MockImplementation.JAVA
    }

    def "nested options carry the deep mock response, the verification and objenesis settings of the parent"() {
        IMockConfiguration parent = Mock()

        when:
        def options = cache.nestedOptions(parent)

        then:
        _ * parent.mockMaker >> null
        _ * parent.verified >> true
        _ * parent.useObjenesis >> false
        _ * cache._
        0 * _

        expect:
        options == [
            defaultResponse: DeepMockSupport.RESPONSE,
            verified       : true,
            useObjenesis   : false,
        ]
    }

    def "nested options carry the parent's mock maker when it has one"() {
        IMockConfiguration parent = Mock()
        IMockMakerSettings mockMaker = Mock()

        when:
        def options = cache.nestedOptions(parent)

        then:
        _ * parent.mockMaker >> mockMaker
        _ * parent.verified >> false
        _ * parent.useObjenesis >> true
        _ * cache._
        0 * _

        expect:
        options == [
            defaultResponse: DeepMockSupport.RESPONSE,
            mockMaker      : mockMaker,
            verified       : false,
            useObjenesis   : true,
        ]
    }

    def "the path name joins the mock name, the method and the described arguments"() {
        Sample child = Mock(name: 'child')

        when:
        def path = cache.pathName(invocation(new Object(), 'person', 'primitive', []))
        def pathWithArguments = cache.pathName(invocation(new Object(), 'person', 'withArguments', [child, 1]))

        then:
        _ * cache._
        0 * _

        expect:
        path == 'person.primitive()'
        pathWithArguments == 'person.withArguments(child, 1)'
    }

    def "a mock is described by its name, or by its type when it has none"() {
        IMockObject named = Mock()
        IMockObject unnamed = Mock()

        when:
        def namedDescription = cache.mockName(named)
        def unnamedDescription = cache.mockName(unnamed)

        then:
        _ * named.name >> 'named'
        _ * unnamed.name >> null
        _ * unnamed.type >> Sample
        _ * cache._
        0 * _

        expect:
        namedDescription == 'named'
        unnamedDescription == 'Sample'
    }

    def "an argument that is not a mock is described by its inspection"() {
        expect:
        cache.describe('text') == "'text'"
        cache.describe(1) == '1'
    }

    def "a call creates a deep mock for the return type and remembers it under its parent"() {
        def created = new Object()
        def parentInstance = new Object()
        Specification specification = Mock()

        when:
        def entry = cache.create(invocation(parentInstance, 'root', 'primitive', [], specification)).get()
        def path = cache.pathTo(created)

        then:
        1 * cache.newMock(specification, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.primitive()') >> created
        _ * cache._
        0 * _

        expect:
        entry.parent.is(parentInstance)
        path == [entry]
    }

    def "a new mock gets the deep mock response, and the parent's verification and objenesis settings"() {
        when:
        cache.create(invocation(new Object(), 'root', 'primitive', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { options ->
            verifyAll(options) {
                defaultResponse.is(DeepMockSupport.RESPONSE)
                !verified
                useObjenesis
                !containsKey('mockMaker')
            }
        }, 'root.primitive()') >> new Object()
        _ * cache._
        0 * _
    }

    def "a call returning a Groovy object creates a Groovy mock"() {
        when:
        cache.create(invocation(new Object(), 'root', 'groovyObject', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, GroovyObject, MockImplementation.GROOVY, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.groovyObject()') >> new Object()
        _ * cache._
        0 * _
    }

    def "a Java mock and a Groovy mock come from different mock implementations"() {
        def options = [defaultResponse: DeepMockSupport.RESPONSE]

        when:
        def javaMock = cache.newMock(this, GroovyNamed, MockImplementation.JAVA, options, 'java')
        def groovyMock = cache.newMock(this, GroovyNamed, MockImplementation.GROOVY, options, 'groovy')

        then:
        _ * cache._
        0 * _

        expect:
        classOf(javaMock) != classOf(groovyMock)
    }

    def "a second call that matches an existing step reuses its mock"() {
        def created = new Object()
        def parentInstance = new Object()

        when:
        def first = cache.findOrCreate(invocation(parentInstance, 'root', 'primitive', []))
        def second = cache.findOrCreate(invocation(parentInstance, 'root', 'primitive', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.primitive()') >> created
        _ * cache._
        0 * _

        expect:
        first.get().is(second.get())
    }

    def "a call that matches a later step returns that step and not the first one"() {
        def firstMock = new Object()
        def secondMock = new Object()
        def firstParent = new Object()
        def secondParent = new Object()

        when:
        cache.create(invocation(firstParent, 'first', 'primitive', []))
        cache.create(invocation(secondParent, 'second', 'array', []))
        def found = cache.findOrCreate(invocation(secondParent, 'second', 'array', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'first.primitive()') >> firstMock
        1 * cache.newMock({ it instanceof Specification }, String[].class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'second.array()') >> secondMock
        _ * cache._
        0 * _

        expect:
        found.get().accept(null).get().is(secondMock)
    }

    def "a mock that cannot be created is not returned and is not attempted again"() {
        when:
        def first = cache.create(invocation(new Object(), 'root', 'primitive', []))
        def second = cache.create(invocation(new Object(), 'root', 'primitive', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.primitive()') >> { throw new CannotCreateMockException(int.class, 'no mock maker') }
        _ * cache._
        0 * _

        expect:
        first.empty
        second.empty
    }

    def "a failed creation does not block a creation for another return type"() {
        when:
        cache.create(invocation(new Object(), 'root', 'primitive', []))
        cache.create(invocation(new Object(), 'root', 'object', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.primitive()') >> { throw new CannotCreateMockException(int.class, 'no mock maker') }
        1 * cache.newMock({ it instanceof Specification }, String, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.object()') >> { throw new CannotCreateMockException(String, 'no mock maker') }
        _ * cache._
        0 * _
    }

    def "a failed creation does not block a creation of another kind"() {
        when:
        cache.create(invocation(new Object(), 'root', 'primitive', []))
        cache.create(invocation(new Object(), 'root', 'groovyObject', []))

        then:
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.primitive()') >> { throw new CannotCreateMockException(int.class, 'no mock maker') }
        1 * cache.newMock({ it instanceof Specification }, GroovyObject, MockImplementation.GROOVY, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.groovyObject()') >> { throw new CannotCreateMockException(GroovyObject, 'no mock maker') }
        _ * cache._
        0 * _
    }

    def "a path to a deep mock lists the steps from the root, and a path to anything else is empty"() {
        def root = new Object()
        def middle = new Object()
        def leaf = new Object()

        when:
        cache.create(invocation(root, 'root', 'withArguments', ['a', 1]))
        cache.create(invocation(middle, 'middle', 'primitive', []))
        def path = cache.pathTo(leaf)
        def emptyPath = cache.pathTo(new Object())

        then:
        1 * cache.newMock({ it instanceof Specification }, String, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, "root.withArguments('a', 1)") >> middle
        1 * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'middle.primitive()') >> leaf
        _ * cache._
        0 * _

        expect:
        path*.text == [
            "_ * root.withArguments('a', 1) (deep mock step of line -1)",
            '_ * middle.primitive() (deep mock step of line -1)',
        ]
        emptyPath.empty
    }

    def "the lookup releases its lock, so another thread can look up a path"() {
        def released = new AtomicBoolean(false)

        when:
        cache.pathTo(new Object())
        Thread.start { cache.pathTo(new Object()); released.set(true) }.join(2000)

        then:
        _ * cache._
        0 * _

        expect:
        released.get()
    }

    def "finding or creating a step releases its lock, so another thread can use the cache"() {
        def released = new AtomicBoolean(false)

        when:
        def first = invocation(new Object(), 'root', 'primitive', [])
        def second = invocation(new Object(), 'root', 'primitive', [])
        cache.findOrCreate(first)
        Thread.start { cache.findOrCreate(second); released.set(true) }.join(2000)

        then:
        _ * cache.newMock({ it instanceof Specification }, int.class, MockImplementation.JAVA, { it.defaultResponse.is(DeepMockSupport.RESPONSE) }, 'root.primitive()') >> new Object()
        _ * cache._
        0 * _

        expect:
        released.get()
    }

    def "an error while finding a step releases the lock for the next caller"() {
        def released = new AtomicBoolean(false)

        when:
        def existing = invocation(new Object(), 'root', 'primitive', [])
        cache.create(existing)
        def failed = throwsNullPointer { cache.findOrCreate(null) }
        Thread.start { cache.findOrCreate(existing); released.set(true) }.join(2000)

        then:
        _ * cache._
        0 * _

        expect:
        failed
        released.get()
    }

    def "a failed creation is remembered, so the same call is not attempted again"() {
        def failing = new FailingCache()

        expect:
        failing.create(invocation(new Object(), 'root', 'primitive', [])).empty
        failing.create(invocation(new Object(), 'root', 'primitive', [])).empty
        failing.attempts == 1
    }

    def "a failed creation does not hide a creation for another return type"() {
        def failing = new FailingCache()

        expect:
        failing.create(invocation(new Object(), 'root', 'primitive', [])).empty
        failing.create(invocation(new Object(), 'root', 'object', [])).empty
        failing.attempts == 2
    }

    def "a failed creation does not hide a creation under another configuration"() {
        def failing = new FailingCache()

        expect:
        failing.create(invocation(new Object(), 'root', 'primitive', [], Mock(Specification), false)).empty
        failing.create(invocation(new Object(), 'root', 'primitive', [], Mock(Specification), true)).empty
        failing.attempts == 2
    }

    private static boolean throwsNullPointer(Closure action) {
        try {
            action()
            return false
        } catch (NullPointerException ignored) {
            return true
        }
    }

    private static Class<?> classOf(Object mock) {
        Object.getMethod('getClass').invoke(mock)
    }

    private IMockInvocation invocation(Object instance, String mockName, String methodName, List<Object> arguments) {
        invocation(instance, mockName, methodName, arguments, Mock(Specification))
    }

    private IMockInvocation invocation(
            Object instance,
            String mockName,
            String methodName,
            List<Object> arguments,
            Specification specification,
            boolean verified = false) {
        IMockInvocation invocation = Mock()
        IMockObject mockObject = Mock()
        IMockConfiguration configuration = Mock()
        configuration.mockMaker >> null
        configuration.verified >> verified
        configuration.useObjenesis >> true
        mockObject.name >> mockName
        mockObject.instance >> instance
        mockObject.matches({ it.is(instance) }) { it == null || it instanceof IMockInteraction } >> true
        mockObject.matches({ !it.is(instance) }) { it == null || it instanceof IMockInteraction } >> false
        mockObject.specification >> specification
        mockObject.configuration >> configuration
        invocation.mockObject >> mockObject
        invocation.method >> Sample.method(methodName)
        invocation.arguments >> arguments
        invocation
    }
}
