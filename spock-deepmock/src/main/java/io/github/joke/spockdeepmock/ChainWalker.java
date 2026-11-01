package io.github.joke.spockdeepmock;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.function.Supplier;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;

/**
 * Answers the calls that a specification makes on deep mocks while it evaluates the target of an interaction.
 * Thread-bound and never counted, so evaluating a chain does not count as an invocation.
 */
class ChainWalker implements IMockInteraction {

    private final Thread thread = currentThread();

    @Override
    public int getLine() {
        return -1;
    }

    @Override
    public int getColumn() {
        return -1;
    }

    @Override
    public String getText() {
        return "deep mock walk";
    }

    @Override
    public boolean matches(final IMockInvocation invocation) {
        return thread.equals(currentThread())
                && invocation.getMockObject().getDefaultResponse() instanceof DeepMockResponse;
    }

    @Override
    public Supplier<Object> accept(final IMockInvocation invocation) {
        return () -> DeepMockSupport.RESPONSE.respond(invocation);
    }

    @Override
    public List<IMockInvocation> getAcceptedInvocations() {
        return emptyList();
    }

    @Override
    public int computeSimilarityScore(final IMockInvocation invocation) {
        return 0;
    }

    @Override
    public String describeMismatch(final IMockInvocation invocation) {
        return "";
    }

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public boolean isExhausted() {
        return false;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
