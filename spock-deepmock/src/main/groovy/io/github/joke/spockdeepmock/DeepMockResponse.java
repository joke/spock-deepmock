package io.github.joke.spockdeepmock;

import groovy.lang.GroovyObject;
import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.MockImplementation;
import spock.lang.Specification;

import java.lang.reflect.Type;

import static org.spockframework.mock.DefaultJavaLangObjectInteractions.INSTANCE;
import static org.spockframework.mock.MockImplementation.GROOVY;
import static org.spockframework.mock.MockImplementation.JAVA;

class DeepMockResponse implements IDefaultResponse {

    private final MockCache mockCache;

    public DeepMockResponse() {
        this.mockCache = new MockCache(this);
    }

    public Object respond(final IMockInvocation invocation) {
        final IMockInteraction interaction = INSTANCE.match(invocation);
        if (interaction != null) {
            return interaction.accept(invocation);
        } else {
            return determineMock(invocation);
        }
    }

    private Object determineMock(final IMockInvocation invocation) {
        final Class<?> returnType = invocation.getMethod().getReturnType();
        final Type exactReturnType = invocation.getMethod().getExactReturnType();
        final Specification specification = invocation.getMockObject().getSpecification();
        final MockImplementation implementation = GroovyObject.class.isAssignableFrom(returnType) ? GROOVY : JAVA;
        final String name = invocation.toString();

        return mockCache.createMock(name, specification, exactReturnType, implementation);
    }
}
