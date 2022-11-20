package io.github.joke.spockdeepmock;

import org.spockframework.mock.IArgumentConstraint;
import org.spockframework.mock.IInvocationConstraint;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.constraint.EqualArgumentConstraint;
import org.spockframework.mock.constraint.EqualMethodNameConstraint;
import org.spockframework.mock.constraint.PositionalArgumentListConstraint;
import org.spockframework.mock.constraint.TargetConstraint;
import org.spockframework.util.UnreachableCodeError;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.Collections.emptyList;

class DeepMockInteraction implements IMockInteraction {

    private final Object mock;

    private final List<IInvocationConstraint> constraints = new ArrayList<>();

    public DeepMockInteraction(final IMockInvocation invocation, final Object mock) {
        this.mock = mock;
        addConstraints(invocation);
    }

    @Override
    public int getLine() {
        throw new UnreachableCodeError("getLine");
    }

    @Override
    public int getColumn() {
        throw new UnreachableCodeError("getColumn");
    }

    @Override
    public String getText() {
        throw new UnreachableCodeError("getText");
    }

    @Override
    public boolean matches(final IMockInvocation invocation) {
        return constraints.stream()
                .allMatch(constraint -> constraint.isSatisfiedBy(invocation));
    }

    @Override
    public Object accept(final IMockInvocation invocation) {
        return this.mock;
    }

    @Override
    public List<IMockInvocation> getAcceptedInvocations() {
        return emptyList();
    }

    @Override
    public int computeSimilarityScore(final IMockInvocation invocation) {
        throw new UnreachableCodeError("computeSimilarityScore");
    }

    @Override
    public String describeMismatch(final IMockInvocation invocation) {
        throw new UnreachableCodeError("describeMismatch");
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

    private void addConstraints(final IMockInvocation invocation) {
        constraints.add(new TargetConstraint(invocation.getMockObject().getInstance()));
        constraints.add(new EqualMethodNameConstraint(invocation.getMethod().getName()));
        final List<IArgumentConstraint> argumentConstraints = invocation.getArguments().stream()
                .map(EqualArgumentConstraint::new)
                .collect(toList());
        constraints.add(new PositionalArgumentListConstraint(argumentConstraints, false));
    }
}
