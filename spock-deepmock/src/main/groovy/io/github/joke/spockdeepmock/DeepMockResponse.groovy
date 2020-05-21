package io.github.joke.spockdeepmock

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.MockImplementation
import spock.lang.Specification

import java.lang.reflect.Type

import static java.lang.Integer.MAX_VALUE
import static org.spockframework.mock.DefaultJavaLangObjectInteractions.INSTANCE
import static org.spockframework.mock.MockImplementation.GROOVY
import static org.spockframework.mock.MockImplementation.JAVA
import static org.spockframework.mock.MockNature.MOCK

@Singleton
@CompileStatic
class DeepMockResponse implements IDefaultResponse {

	Object respond(IMockInvocation invocation) {
		def interaction = INSTANCE.match invocation

		if (interaction) {
			interaction.accept invocation
		} else {
			def returnType = invocation.method.returnType
			def exactReturnType = invocation.method.exactReturnType
			def specification = invocation.mockObject.specification
			def implementation = GroovyObject.isAssignableFrom(returnType) ? GROOVY : JAVA
			def name = "$invocation"

			createMock name, specification, exactReturnType, implementation
		}
	}

	@Memoized(protectedCacheSize = MAX_VALUE)
	private Object createMock(String name, Specification specification, Type exactReturnType, MockImplementation implementation) {
		specification.createMock name, exactReturnType, MOCK, implementation, [defaultResponse: DeepMockResponse.instance] as Map<String, Object>, null
	}
}
