package io.github.joke.spockdeepmock;

import org.spockframework.mock.DefaultJavaLangObjectInteractions;
import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.ZeroOrNullResponse;
import org.spockframework.mock.runtime.MockController;
import org.spockframework.runtime.extension.IStore;

import java.util.NoSuchElementException;
import java.util.Optional;

class DeepMockResponse implements IDefaultResponse {

    public Object respond(final IMockInvocation invocation) {
        final IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
        if (interaction != null) {
            return interaction.accept(invocation).get();
        }
        if (isNeverMockable(invocation)) {
            return fallback(invocation);
        }
        final Optional<DeepMockInteraction> entry = mockCache(invocation).findOrCreate(invocation);
        if (!entry.isPresent()) {
            return fallback(invocation);
        }
        addInteraction(invocation, entry.get());
        return entry.get().accept(invocation).get();
    }

    private static boolean isNeverMockable(final IMockInvocation invocation) {
        final Class<?> returnType = invocation.getMethod().getReturnType();
        return returnType.isPrimitive() || returnType.isArray();
    }

    private static Object fallback(final IMockInvocation invocation) {
        return ZeroOrNullResponse.INSTANCE.respond(invocation);
    }

    private MockCache mockCache(final IMockInvocation invocation) {
        return invocation.getMockObject().getSpecification().getSpecificationContext()
                .getStore(IStore.Namespace.create(MockCache.class))
                .getOrComputeIfAbsent(MockCache.class, key -> new MockCache(this), MockCache.class);
    }

    private void addInteraction(final IMockInvocation invocation, final DeepMockInteraction entry) {
        final MockController mockController = (MockController) invocation.getMockObject()
                .getSpecification().getSpecificationContext().getMockController();
        try {
            mockController.addInteraction(entry);
        } catch (final NoSuchElementException noInteractionScope) {
            // Spock has no interaction scope in cleanup blocks and cleanup(): the call
            // is not matched against interactions there, so there is nothing to register.
        }
    }
}
