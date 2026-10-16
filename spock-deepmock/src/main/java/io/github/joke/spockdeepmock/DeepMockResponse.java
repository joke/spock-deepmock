package io.github.joke.spockdeepmock;

import org.spockframework.mock.DefaultJavaLangObjectInteractions;
import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.ZeroOrNullResponse;

class DeepMockResponse implements IDefaultResponse {

    public Object respond(final IMockInvocation invocation) {
        final IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
        if (interaction != null) {
            return interaction.accept(invocation).get();
        }
        if (isNeverMockable(invocation)) {
            return fallback(invocation);
        }
        return MockCache.of(invocation.getMockObject().getSpecification())
                .findOrCreate(invocation)
                .map(entry -> entry.accept(invocation).get())
                .orElseGet(() -> fallback(invocation));
    }

    private static boolean isNeverMockable(final IMockInvocation invocation) {
        final Class<?> returnType = invocation.getMethod().getReturnType();
        return returnType.isPrimitive() || returnType.isArray();
    }

    private static Object fallback(final IMockInvocation invocation) {
        return ZeroOrNullResponse.INSTANCE.respond(invocation);
    }
}
