package io.github.joke.spockdeepmock;

import groovy.lang.GroovyObject;
import org.spockframework.mock.DefaultJavaLangObjectInteractions;
import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.MockImplementation;
import org.spockframework.mock.runtime.MockController;
import spock.lang.Specification;

import java.lang.reflect.Type;

import static org.spockframework.mock.MockImplementation.GROOVY;
import static org.spockframework.mock.MockImplementation.JAVA;

class DeepMockResponse implements IDefaultResponse {

    private final MockCache mockCache;

    public DeepMockResponse() {
        this.mockCache = new MockCache(this);
    }

    public Object respond(final IMockInvocation invocation) {
        final IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
        if (interaction != null) {
            return interaction.accept(invocation);
        } else {
            final Object mock = createCachedMock(invocation);
            addInteraction(invocation, mock);
            return mock;
        }
    }

    private void addInteraction(final IMockInvocation invocation, final Object mock) {
        final MockController mockController = (MockController) invocation.getMockObject()
                .getSpecification().getSpecificationContext().getMockController();
        mockController.addInteraction(new DeepMockInteraction(invocation, mock));
    }

    private Object createCachedMock(final IMockInvocation invocation) {
        final Class<?> returnType = invocation.getMethod().getReturnType();
        final Type exactReturnType = invocation.getMethod().getExactReturnType();
        final Specification specification = invocation.getMockObject().getSpecification();
        final MockImplementation implementation = GroovyObject.class.isAssignableFrom(returnType) ? GROOVY : JAVA;
        final IMockObject iMockObject = invocation.getMockObject();

        return mockCache.createMock(iMockObject, specification, exactReturnType, implementation);
    }
}
