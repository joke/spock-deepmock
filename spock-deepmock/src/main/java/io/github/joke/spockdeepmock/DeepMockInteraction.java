package io.github.joke.spockdeepmock;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.spockframework.mock.IArgumentConstraint;
import org.spockframework.mock.IInvocationConstraint;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.constraint.EqualArgumentConstraint;
import org.spockframework.mock.constraint.EqualMethodNameConstraint;
import org.spockframework.mock.constraint.PositionalArgumentListConstraint;
import org.spockframework.mock.constraint.TargetConstraint;
import org.spockframework.util.UnreachableCodeError;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class DeepMockInteraction implements IMockInteraction {

    private final Object mock;

    private final List<IInvocationConstraint> constraints;

    static DeepMockInteraction create(final IMockInvocation invocation, final Object mock) {
        return new DeepMockInteraction(mock, constraints(invocation));
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
    public Supplier<Object> accept(final IMockInvocation invocation) {
        return () -> this.mock;
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

    private static List<IInvocationConstraint> constraints(final IMockInvocation invocation) {
        final List<IArgumentConstraint> argumentConstraints = invocation.getArguments().stream()
                .map(EqualArgumentConstraint::new)
                .collect(toList());
        return asList(
                new TargetConstraint(invocation.getMockObject().getInstance()),
                new EqualMethodNameConstraint(invocation.getMethod().getName()),
                new ParameterTypesConstraint(invocation.getMethod().getParameterTypes()),
                new PositionalArgumentListConstraint(argumentConstraints, false));
    }

    private static final class ParameterTypesConstraint implements IInvocationConstraint {

        private final List<Class<?>> parameterTypes;

        ParameterTypesConstraint(final List<Class<?>> parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean isSatisfiedBy(final IMockInvocation invocation) {
            return parameterTypes.equals(invocation.getMethod().getParameterTypes());
        }

        @Override
        public String describeMismatch(final IMockInvocation invocation) {
            return "parameter types " + parameterTypes + " do not match " + invocation.getMethod().getParameterTypes();
        }
    }
}
