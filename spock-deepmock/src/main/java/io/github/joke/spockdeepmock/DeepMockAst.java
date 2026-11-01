package io.github.joke.spockdeepmock;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

/**
 * Builds and inspects the Groovy AST nodes that {@link DeepMockTransformation} rewrites.
 */
final class DeepMockAst {

    private static final String DEFAULT_RESPONSE = "defaultResponse";

    private static final int SINGLE_ARGUMENT = 1;

    private DeepMockAst() {}

    @VisibleForTesting
    static boolean isResponse(final BinaryExpression expression) {
        final int type = expression.getOperation().getType();
        return type == RIGHT_SHIFT || type == RIGHT_SHIFT_UNSIGNED;
    }

    @VisibleForTesting
    static Expression stripResponses(final Expression expression) {
        Expression call = expression;
        while (call instanceof BinaryExpression && isResponse((BinaryExpression) call)) {
            call = ((BinaryExpression) call).getLeftExpression();
        }
        return call;
    }

    /**
     * Returns the call that an interaction statement declares, or {@code null} when the statement is not an
     * interaction. An interaction is {@code target * call >> response}, with the response and the cardinality optional.
     */
    @VisibleForTesting
    static @Nullable Expression interactionCall(final Expression expression) {
        final Expression call = stripResponses(expression);
        if (isMultiply(call)) {
            return ((BinaryExpression) call).getRightExpression();
        }
        return hasResponse(expression) ? call : null;
    }

    @VisibleForTesting
    static boolean hasResponse(final Expression expression) {
        return expression instanceof BinaryExpression && isResponse((BinaryExpression) expression);
    }

    @VisibleForTesting
    static boolean isMultiply(final Expression expression) {
        return expression instanceof BinaryExpression
                && ((BinaryExpression) expression).getOperation().getType() == MULTIPLY;
    }

    /**
     * Returns the expression at the root of a property or method chain, or {@code null} when the chain starts at an
     * implicit this.
     */
    @VisibleForTesting
    static @Nullable Expression rootOf(final Expression target) {
        Expression root = target;
        while (isChain(root)) {
            if (isImplicitThis(root)) {
                return null;
            }
            root = objectOf(root);
        }
        return root;
    }

    @VisibleForTesting
    static boolean isChain(final Expression expression) {
        return expression instanceof PropertyExpression || expression instanceof MethodCallExpression;
    }

    @VisibleForTesting
    static boolean isImplicitThis(final Expression expression) {
        return expression instanceof PropertyExpression
                ? ((PropertyExpression) expression).isImplicitThis()
                : ((MethodCallExpression) expression).isImplicitThis();
    }

    @VisibleForTesting
    static Expression objectOf(final Expression expression) {
        return expression instanceof PropertyExpression
                ? ((PropertyExpression) expression).getObjectExpression()
                : ((MethodCallExpression) expression).getObjectExpression();
    }

    @VisibleForTesting
    static Expression wrap(final Expression target, final int line, final int column) {
        final MethodCallExpression begin =
                staticCall("beginWalk", THIS_EXPRESSION, new ConstantExpression(line), new ConstantExpression(column));
        final MethodCallExpression end = staticCall("endWalk", begin, target);
        begin.setSourcePosition(target);
        end.setSourcePosition(target);
        return end;
    }

    @VisibleForTesting
    static boolean isDeepMockCall(final MethodCallExpression call) {
        return isDeepMockName(call.getMethodAsString())
                && (call.isImplicitThis() || isThis(call.getObjectExpression()));
    }

    @VisibleForTesting
    static boolean isDeepMockName(final String name) {
        return "DeepMock".equals(name) || "GroovyDeepMock".equals(name);
    }

    @VisibleForTesting
    static boolean isThis(final Expression expression) {
        return expression instanceof VariableExpression && ((VariableExpression) expression).isThisExpression();
    }

    @VisibleForTesting
    static void rewrite(final MethodCallExpression call) {
        final boolean groovy = "GroovyDeepMock".equals(call.getMethodAsString());
        call.setMethod(new ConstantExpression(groovy ? "GroovyMock" : "Mock"));

        final List<Expression> arguments = new ArrayList<>(((TupleExpression) call.getArguments()).getExpressions());
        final ClosureExpression closure = trailingClosure(arguments);
        final List<Expression> rewritten = new ArrayList<>(leadingArguments(arguments, closure));
        rewriteLeading(rewritten);
        if (closure != null) {
            rewritten.add(closure);
        }
        call.setArguments(new ArgumentListExpression(rewritten));
    }

    @VisibleForTesting
    static @Nullable ClosureExpression trailingClosure(final List<Expression> arguments) {
        if (arguments.isEmpty()) {
            return null;
        }
        final Expression last = arguments.get(arguments.size() - 1);
        return last instanceof ClosureExpression ? (ClosureExpression) last : null;
    }

    @VisibleForTesting
    static List<Expression> leadingArguments(
            final List<Expression> arguments, final @Nullable ClosureExpression closure) {
        return closure == null ? arguments : arguments.subList(0, arguments.size() - 1);
    }

    @VisibleForTesting
    static void rewriteLeading(final List<Expression> leading) {
        if (leading.isEmpty() || leading.get(0) instanceof ClassExpression) {
            leading.add(0, defaultResponseMap());
        } else if (leading.get(0) instanceof MapExpression) {
            addDefaultResponseEntry((MapExpression) leading.get(0));
        } else {
            leading.set(0, passedArgument(leading));
        }
    }

    /**
     * The first argument, when it is not a class or a map: one argument is spread as the arguments of a call, and
     * several arguments are passed as options.
     */
    @VisibleForTesting
    static Expression passedArgument(final List<Expression> leading) {
        return leading.size() == SINGLE_ARGUMENT
                ? new SpreadExpression(staticCall("arguments", leading.get(0)))
                : staticCall("options", leading.get(0));
    }

    @VisibleForTesting
    static MapExpression defaultResponseMap() {
        final MapExpression map = new MapExpression();
        addDefaultResponseEntry(map);
        return map;
    }

    @VisibleForTesting
    static void addDefaultResponseEntry(final MapExpression map) {
        final boolean declared = map.getMapEntryExpressions().stream().anyMatch(DeepMockAst::isDefaultResponseEntry);
        if (!declared) {
            map.addMapEntryExpression(new ConstantExpression(DEFAULT_RESPONSE), supportField("RESPONSE"));
            return;
        }
        map.getMapEntryExpressions().stream()
                .filter(DeepMockAst::isDefaultResponseEntry)
                .forEach(entry -> entry.setValueExpression(staticCall("rejectDefaultResponse")));
    }

    @VisibleForTesting
    static boolean isDefaultResponseEntry(final MapEntryExpression entry) {
        return entry.getKeyExpression() instanceof ConstantExpression
                && DEFAULT_RESPONSE.equals(((ConstantExpression) entry.getKeyExpression()).getValue());
    }

    @VisibleForTesting
    static PropertyExpression supportField(final String name) {
        return new PropertyExpression(supportClass(), name);
    }

    @VisibleForTesting
    static MethodCallExpression staticCall(final String method, final Expression... arguments) {
        final MethodCallExpression call =
                new MethodCallExpression(supportClass(), method, new ArgumentListExpression(arguments));
        call.setImplicitThis(false);
        return call;
    }

    @VisibleForTesting
    static ClassExpression supportClass() {
        return new ClassExpression(make(DeepMockSupport.class));
    }
}
