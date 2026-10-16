package io.github.joke.spockdeepmock;

import groovy.lang.GroovyObject;
import lombok.RequiredArgsConstructor;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockConfiguration;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.MockImplementation;
import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.SpecInternals;
import spock.lang.Specification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.spockframework.mock.MockImplementation.GROOVY;
import static org.spockframework.mock.MockImplementation.JAVA;

@RequiredArgsConstructor
class MockCache {

    private static final MockUtil MOCK_UTIL = new MockUtil();

    private final DeepMockResponse deepMockResponse;

    private final List<DeepMockInteraction> entries = new ArrayList<>();

    private final Set<List<Object>> failedCreations = new HashSet<>();

    synchronized Optional<DeepMockInteraction> findOrCreate(final IMockInvocation invocation) {
        final Optional<DeepMockInteraction> existing = entries.stream()
                .filter(entry -> entry.matches(invocation))
                .findFirst();
        return existing.isPresent() ? existing : create(invocation);
    }

    private Optional<DeepMockInteraction> create(final IMockInvocation invocation) {
        final IMockObject mockObject = invocation.getMockObject();
        final Specification specification = mockObject.getSpecification();
        final Type exactReturnType = invocation.getMethod().getExactReturnType();
        final MockImplementation implementation = implementationOf(invocation);
        final Map<String, Object> options = nestedOptions(mockObject.getConfiguration());
        final List<Object> key = Arrays.asList(exactReturnType, implementation, options);
        if (failedCreations.contains(key)) {
            return Optional.empty();
        }
        final Object mock;
        try {
            mock = newMock(specification, exactReturnType, implementation, options, pathName(invocation));
        } catch (final CannotCreateMockException cannotCreate) {
            failedCreations.add(key);
            return Optional.empty();
        }
        final DeepMockInteraction entry = DeepMockInteraction.create(invocation, mock);
        entries.add(entry);
        return Optional.of(entry);
    }

    private static MockImplementation implementationOf(final IMockInvocation invocation) {
        return GroovyObject.class.isAssignableFrom(invocation.getMethod().getReturnType()) ? GROOVY : JAVA;
    }

    private Map<String, Object> nestedOptions(final IMockConfiguration parent) {
        final Map<String, Object> options = new LinkedHashMap<>();
        options.put("defaultResponse", deepMockResponse);
        if (parent.getMockMaker() != null) {
            options.put("mockMaker", parent.getMockMaker());
        }
        options.put("verified", parent.isVerified());
        options.put("useObjenesis", parent.isUseObjenesis());
        return options;
    }

    private static String pathName(final IMockInvocation invocation) {
        final String arguments = invocation.getArguments().stream()
                .map(MockCache::describe)
                .collect(joining(", "));
        return mockName(invocation.getMockObject()) + "." + invocation.getMethod().getName() + "(" + arguments + ")";
    }

    private static String describe(final Object argument) {
        return MOCK_UTIL.isMock(argument)
                ? mockName(MOCK_UTIL.asMock(argument))
                : InvokerHelper.inspect(argument);
    }

    private static String mockName(final IMockObject mockObject) {
        return mockObject.getName() != null ? mockObject.getName() : mockObject.getType().getSimpleName();
    }

    private static Object newMock(final Specification specification, final Type exactReturnType, final MockImplementation implementation, final Map<String, Object> options, final String name) {
        return implementation == GROOVY
                ? SpecInternals.GroovyMockImpl(specification, name, exactReturnType, options)
                : SpecInternals.MockImpl(specification, name, exactReturnType, options);
    }
}
