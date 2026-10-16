package io.github.joke.spockdeepmock;

import org.spockframework.mock.DefaultJavaLangObjectInteractions;
import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.runtime.MockController;
import org.spockframework.runtime.extension.IStore;

import java.util.NoSuchElementException;

class DeepMockResponse implements IDefaultResponse {

    public Object respond(final IMockInvocation invocation) {
        final IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
        if (interaction != null) {
            return interaction.accept(invocation).get();
        }
        final DeepMockInteraction entry = mockCache(invocation).findOrCreate(invocation);
        addInteraction(invocation, entry);
        return entry.accept(invocation).get();
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
