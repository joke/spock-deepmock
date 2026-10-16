package io.github.joke.spockdeepmock;

import groovy.lang.GroovyObject;
import lombok.RequiredArgsConstructor;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.MockImplementation;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.SpecInternals;
import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.spockframework.mock.MockImplementation.GROOVY;
import static org.spockframework.mock.MockImplementation.JAVA;

@RequiredArgsConstructor
class MockCache {

    private final DeepMockResponse deepMockResponse;

    private final List<DeepMockInteraction> entries = new ArrayList<>();

    synchronized DeepMockInteraction findOrCreate(final IMockInvocation invocation) {
        return entries.stream()
                .filter(entry -> entry.matches(invocation))
                .findFirst()
                .orElseGet(() -> create(invocation));
    }

    private DeepMockInteraction create(final IMockInvocation invocation) {
        requireMockable(invocation);
        final IMockObject mockObject = invocation.getMockObject();
        final Specification specification = mockObject.getSpecification();
        final Type exactReturnType = invocation.getMethod().getExactReturnType();
        final Object mock = newMock(mockObject, specification, exactReturnType, implementationOf(invocation));
        final DeepMockInteraction entry = DeepMockInteraction.create(invocation, mock);
        entries.add(entry);
        return entry;
    }

    private static void requireMockable(final IMockInvocation invocation) {
        final Class<?> returnType = invocation.getMethod().getReturnType();
        final String reason = unmockableReason(returnType);
        if (reason != null) {
            throw new InvalidSpecException(String.format(
                    "Cannot return a deep mock from %s.%s(): %s %s and cannot be mocked. "
                            + "Stub the call instead, for example: %s.%s() >> <value>",
                    invocation.getMockObject().getName(), invocation.getMethod().getName(),
                    returnType.getName(), reason,
                    invocation.getMockObject().getName(), invocation.getMethod().getName()));
        }
    }

    private static String unmockableReason(final Class<?> type) {
        if (type.isPrimitive()) {
            return "is a primitive type";
        }
        if (type.isArray()) {
            return "is an array";
        }
        if (Modifier.isFinal(type.getModifiers())) {
            return "is final";
        }
        return null;
    }

    private static MockImplementation implementationOf(final IMockInvocation invocation) {
        return GroovyObject.class.isAssignableFrom(invocation.getMethod().getReturnType()) ? GROOVY : JAVA;
    }

    private Object newMock(final IMockObject mockObject, final Specification specification, final Type exactReturnType, final MockImplementation implementation) {
        final Map<String, Object> options = singletonMap("defaultResponse", deepMockResponse);
        return implementation == GROOVY
                ? SpecInternals.GroovyMockImpl(specification, mockObject.getName(), exactReturnType, options)
                : SpecInternals.MockImpl(specification, mockObject.getName(), exactReturnType, options);
    }
}
