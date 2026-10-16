package io.github.joke.spockdeepmock;

import org.spockframework.mock.IDefaultResponse;
import org.spockframework.mock.runtime.MockController;
import spock.lang.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime support for the code that {@link DeepMockTransformation} generates. Not intended for direct use.
 */
public final class DeepMockSupport {

    public static final IDefaultResponse RESPONSE = new DeepMockResponse();

    private static final String DEFAULT_RESPONSE = "defaultResponse";

    private DeepMockSupport() {
    }

    public static Map<String, Object> options(final Object options) {
        // generated code passes the caller's options map as Object
        @SuppressWarnings("unchecked")
        final Map<String, Object> callerOptions = (Map<String, Object>) options;
        final Map<String, Object> copy = new LinkedHashMap<>(callerOptions);
        if (copy.containsKey(DEFAULT_RESPONSE)) {
            throw defaultResponseRejected();
        }
        copy.put(DEFAULT_RESPONSE, RESPONSE);
        return copy;
    }

    public static Object rejectDefaultResponse() {
        throw defaultResponseRejected();
    }

    /**
     * Starts a walk over the target of an interaction. Calls on deep mocks made until {@link #endWalk} are answered
     * without counting. Returns {@code null} outside a specification.
     */
    public static Object beginWalk(final Object self, final int line, final int column) {
        if (!(self instanceof Specification)) {
            return null;
        }
        final Specification specification = (Specification) self;
        final MockController controller = (MockController) specification.getSpecificationContext().getMockController();
        controller.enterScope();
        controller.addInteraction(new ChainWalker());
        return new Walk(specification, controller, line, column);
    }

    /**
     * Ends a walk and registers the implicit step interactions of the target, root first. Returns the target.
     */
    public static <T> T endWalk(final Object walk, final T target) {
        if (!(walk instanceof Walk)) {
            return target;
        }
        final Walk current = (Walk) walk;
        current.controller.leaveScope();
        for (final DeepMockInteraction step : MockCache.of(current.specification).pathTo(target)) {
            current.controller.addInteraction(step.stepAt(current.line, current.column));
        }
        return target;
    }

    public static List<Object> arguments(final Object argument) {
        if (argument instanceof Map) {
            return new ArrayList<>(Arrays.asList(options(argument)));
        }
        final Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(DEFAULT_RESPONSE, RESPONSE);
        return new ArrayList<>(Arrays.asList(defaults, argument));
    }

    private static UnsupportedOperationException defaultResponseRejected() {
        return new UnsupportedOperationException("DeepMocks with default response are not supported.");
    }

    private static final class Walk {

        private final Specification specification;

        private final MockController controller;

        private final int line;

        private final int column;

        Walk(final Specification specification, final MockController controller, final int line, final int column) {
            this.specification = specification;
            this.controller = controller;
            this.line = line;
            this.column = column;
        }
    }
}
