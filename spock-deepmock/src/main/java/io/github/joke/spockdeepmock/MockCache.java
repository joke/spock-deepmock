package io.github.joke.spockdeepmock;

import static java.util.stream.Collectors.joining;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.inspect;
import static org.spockframework.mock.MockImplementation.GROOVY;
import static org.spockframework.mock.MockImplementation.JAVA;
import static org.spockframework.runtime.SpecInternals.GroovyMockImpl;
import static org.spockframework.runtime.SpecInternals.MockImpl;

import groovy.lang.GroovyObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.VisibleForTesting;
import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.IMockConfiguration;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.MockImplementation;
import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.extension.IStore;
import spock.lang.Specification;

@RequiredArgsConstructor
class MockCache {

    private static final MockUtil MOCK_UTIL = new MockUtil();

    private final IDefaultResponse deepMockResponse;

    private final List<DeepMockInteraction> entries = new ArrayList<>();

    private final Map<Object, DeepMockInteraction> entriesByMock = new IdentityHashMap<>();

    private final List<FailedCreation> failedCreations = new ArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();

    @VisibleForTesting
    static MockCache of(final Specification specification) {
        return specification
                .getSpecificationContext()
                .getStore(IStore.Namespace.create(MockCache.class))
                .getOrComputeIfAbsent(MockCache.class, key -> new MockCache(DeepMockSupport.RESPONSE), MockCache.class);
    }

    /**
     * Returns the implicit step interactions of the chain that leads from the root deep mock to the target, root first.
     * Returns an empty list when the target is not a nested deep mock of this cache.
     */
    @VisibleForTesting
    List<DeepMockInteraction> pathTo(final Object target) {
        return locked(() -> chainTo(target));
    }

    @VisibleForTesting
    List<DeepMockInteraction> chainTo(final Object target) {
        final List<DeepMockInteraction> path = new ArrayList<>();
        DeepMockInteraction entry = entriesByMock.get(target);
        while (entry != null) {
            path.add(0, entry);
            entry = entriesByMock.get(entry.getParent());
        }
        return path;
    }

    @VisibleForTesting
    Optional<DeepMockInteraction> findOrCreate(final IMockInvocation invocation) {
        return locked(() -> findExistingOrCreate(invocation));
    }

    @VisibleForTesting
    Optional<DeepMockInteraction> findExistingOrCreate(final IMockInvocation invocation) {
        final Optional<DeepMockInteraction> existing =
                entries.stream().filter(entry -> entry.matches(invocation)).findFirst();
        return existing.isPresent() ? existing : create(invocation);
    }

    @VisibleForTesting
    <T> T locked(final Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    @VisibleForTesting
    Optional<DeepMockInteraction> create(final IMockInvocation invocation) {
        final Type exactReturnType = invocation.getMethod().getExactReturnType();
        final MockImplementation implementation = implementationOf(invocation);
        final Map<String, Object> options =
                nestedOptions(invocation.getMockObject().getConfiguration());
        if (hasFailed(exactReturnType, options)) {
            return Optional.empty();
        }
        return newMockOrRemember(invocation, exactReturnType, implementation, options)
                .map(mock -> register(invocation, mock));
    }

    @VisibleForTesting
    boolean hasFailed(final Type exactReturnType, final Map<String, Object> options) {
        return failedCreations.stream().anyMatch(failed -> failed.matches(exactReturnType, options));
    }

    @VisibleForTesting
    Optional<Object> newMockOrRemember(
            final IMockInvocation invocation,
            final Type exactReturnType,
            final MockImplementation implementation,
            final Map<String, Object> options) {
        try {
            return Optional.of(newMock(
                    invocation.getMockObject().getSpecification(),
                    exactReturnType,
                    implementation,
                    options,
                    pathName(invocation)));
        } catch (final CannotCreateMockException cannotCreate) {
            failedCreations.add(new FailedCreation(exactReturnType, options));
            return Optional.empty();
        }
    }

    @VisibleForTesting
    DeepMockInteraction register(final IMockInvocation invocation, final Object mock) {
        final DeepMockInteraction entry = DeepMockInteraction.create(invocation, mock, pathName(invocation));
        entries.add(entry);
        entriesByMock.put(mock, entry);
        return entry;
    }

    @VisibleForTesting
    MockImplementation implementationOf(final IMockInvocation invocation) {
        return GroovyObject.class.isAssignableFrom(invocation.getMethod().getReturnType()) ? GROOVY : JAVA;
    }

    @VisibleForTesting
    Map<String, Object> nestedOptions(final IMockConfiguration parent) {
        final Map<String, Object> options = new LinkedHashMap<>();
        options.put("defaultResponse", deepMockResponse);
        if (parent.getMockMaker() != null) {
            options.put("mockMaker", parent.getMockMaker());
        }
        options.put("verified", parent.isVerified());
        options.put("useObjenesis", parent.isUseObjenesis());
        return options;
    }

    @VisibleForTesting
    String pathName(final IMockInvocation invocation) {
        final String arguments =
                invocation.getArguments().stream().map(this::describe).collect(joining(", "));
        return mockName(invocation.getMockObject()) + "."
                + invocation.getMethod().getName() + "(" + arguments + ")";
    }

    @VisibleForTesting
    String describe(final Object argument) {
        return MOCK_UTIL.isMock(argument) ? mockName(MOCK_UTIL.asMock(argument)) : inspect(argument);
    }

    @VisibleForTesting
    String mockName(final IMockObject mockObject) {
        return mockObject.getName() != null
                ? mockObject.getName()
                : mockObject.getType().getSimpleName();
    }

    @VisibleForTesting
    Object newMock(
            final Specification specification,
            final Type exactReturnType,
            final MockImplementation implementation,
            final Map<String, Object> options,
            final String name) {
        return implementation == GROOVY
                ? GroovyMockImpl(specification, name, exactReturnType, options)
                : MockImpl(specification, name, exactReturnType, options);
    }

    /**
     * A mock creation that Spock refused, so the same call is not attempted again. The implementation is not part of
     * the key: it follows from the exact return type.
     */
    @RequiredArgsConstructor
    private static final class FailedCreation {

        private final Type exactReturnType;

        private final Map<String, Object> options;

        @VisibleForTesting
        boolean matches(final Type exactReturnType, final Map<String, Object> options) {
            return this.exactReturnType.equals(exactReturnType) && this.options.equals(options);
        }
    }
}
