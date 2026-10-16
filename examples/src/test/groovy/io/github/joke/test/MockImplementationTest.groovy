package io.github.joke.test

import org.spockframework.mock.MockImplementation
import org.spockframework.mock.MockUtil
import spock.lang.Specification

class MockImplementationTest extends Specification {

    def mockUtil = new MockUtil()

    Nested javaNested = DeepMock()
    GroovyNested groovyNested = GroovyDeepMock()

    def 'top-level DeepMock is a Java mock'() {
        expect:
        mockUtil.asMock(javaNested).getConfiguration().getImplementation() == MockImplementation.JAVA
    }

    def 'top-level GroovyDeepMock is a Groovy mock'() {
        expect:
        mockUtil.asMock(groovyNested).getConfiguration().getImplementation() == MockImplementation.GROOVY
    }

    def 'nested mock of a Java class is a Java mock'() {
        expect:
        mockUtil.asMock(javaNested.child).getConfiguration().getImplementation() == MockImplementation.JAVA
    }

    def 'nested mock of a Groovy class is a Groovy mock'() {
        expect:
        mockUtil.asMock(groovyNested.child).getConfiguration().getImplementation() == MockImplementation.GROOVY
    }
}
