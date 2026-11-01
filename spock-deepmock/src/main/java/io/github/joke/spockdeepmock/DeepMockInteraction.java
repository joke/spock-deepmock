package io.github.joke.spockdeepmock;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.VisibleForTesting;
import org.spockframework.mock.IArgumentConstraint;
import org.spockframework.mock.IInvocationConstraint;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.constraint.EqualArgumentConstraint;
import org.spockframework.mock.constraint.EqualMethodNameConstraint;
import org.spockframework.mock.constraint.PositionalArgumentListConstraint;
import org.spockframework.mock.constraint.TargetConstraint;

/**
 * The implicit step interaction of a deep mock chain: {@code _ * parent.call(arguments) >> nested}.
 * It is registered for each step of a chain, at the position of the declaring interaction. It never
 * counts invocations.
 */
final class DeepMockInteraction implements IMockInteraction {

    private final Object mock;

    private final Object parent;

    private final String pathName;

    private final List<IInvocationConstraint> constraints;

    private final int line;

    private final int column;

    private DeepMockInteraction(
            final Object mock,
            final Object parent,
            final String pathName,
            final List<IInvocationConstraint> constraints,
            final int line,
            final int column) {
        this.mock = mock;
        this.parent = parent;
        this.pathName = pathName;
        this.constraints = constraints;
        this.line = line;
        this.column = column;
    }

    @VisibleForTesting
    static DeepMockInteraction create(final IMockInvocation invocation, final Object mock, final String pathName) {
        final List<IArgumentConstraint> argumentConstraints = invocation.getArguments().stream()
                .map(EqualArgumentConstraint::new)
                .collect(toList());
        final List<IInvocationConstraint> constraints = asList(
                new TargetConstraint(invocation.getMockObject().getInstance()),
                new EqualMethodNameConstraint(invocation.getMethod().getName()),
                new ParameterTypesConstraint(invocation.getMethod().getParameterTypes()),
                new PositionalArgumentListConstraint(argumentConstraints, false));
        return new DeepMockInteraction(mock, invocation.getMockObject().getInstance(), pathName, constraints, -1, -1);
    }

    @VisibleForTesting
    DeepMockInteraction stepAt(final int line, final int column) {
        return new DeepMockInteraction(mock, parent, pathName, constraints, line, column);
    }

    @VisibleForTesting
    Object getParent() {
        return parent;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public String getText() {
        return "_ * " + pathName + " (deep mock step of line " + line + ")";
    }

    @Override
    public boolean matches(final IMockInvocation invocation) {
        return constraints.stream().allMatch(constraint -> constraint.isSatisfiedBy(invocation));
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
        int score = 0;
        int weight = constraints.size();
        for (final IInvocationConstraint constraint : constraints) {
            if (!constraint.isSatisfiedBy(invocation)) {
                score += weight;
            }
            weight--;
        }
        return score;
    }

    @Override
    public String describeMismatch(final IMockInvocation invocation) {
        final List<String> mismatches = new ArrayList<>();
        for (final IInvocationConstraint constraint : constraints) {
            if (!constraint.isSatisfiedBy(invocation)) {
                mismatches.add(constraint.describeMismatch(invocation));
            }
        }
        return join("\n", mismatches);
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

    @Override
    public String toString() {
        return getText();
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
            return "parameter types " + parameterTypes + " do not match "
                    + invocation.getMethod().getParameterTypes();
        }
    }
}
