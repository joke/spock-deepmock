package io.github.joke.spockdeepmock;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
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
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.control.CompilePhase.CONVERSION;

/**
 * Rewrites {@code DeepMock(...)} and {@code GroovyDeepMock(...)} calls into {@code Mock(...)} and {@code GroovyMock(...)}
 * calls with the deep mock response in the options, before Spock's semantic analysis sees them.
 */
@GroovyASTTransformation(phase = CONVERSION)
public class DeepMockTransformation implements ASTTransformation {

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        for (final ClassNode classNode : source.getAST().getClasses()) {
            new Rewriter(source).visitClass(classNode);
        }
    }

    private static final class Rewriter extends ClassCodeVisitorSupport {

        private static final String DEFAULT_RESPONSE = "defaultResponse";

        private final SourceUnit source;

        private boolean inStaticMethod;

        private int closureDepth;

        Rewriter(final SourceUnit source) {
            this.source = source;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return source;
        }

        @Override
        public void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
            final boolean outerStatic = inStaticMethod;
            inStaticMethod = node.isStatic();
            try {
                super.visitConstructorOrMethod(node, isConstructor);
            } finally {
                inStaticMethod = outerStatic;
            }
        }

        @Override
        public void visitClosureExpression(final ClosureExpression expression) {
            closureDepth++;
            try {
                super.visitClosureExpression(expression);
            } finally {
                closureDepth--;
            }
        }

        @Override
        public void visitExpressionStatement(final ExpressionStatement statement) {
            if (!inStaticMethod) {
                wrapInteractionTarget(statement);
            }
            super.visitExpressionStatement(statement);
        }

        private void wrapInteractionTarget(final ExpressionStatement statement) {
            final Expression expression = statement.getExpression();
            final int line = expression.getLineNumber();
            final int column = expression.getColumnNumber();
            Expression call = expression;
            boolean interaction = false;
            while (call instanceof BinaryExpression && isResponse((BinaryExpression) call)) {
                call = ((BinaryExpression) call).getLeftExpression();
                interaction = true;
            }
            if (call instanceof BinaryExpression && ((BinaryExpression) call).getOperation().getType() == Types.MULTIPLY) {
                call = ((BinaryExpression) call).getRightExpression();
                interaction = true;
            }
            if (!interaction) {
                return;
            }
            if (call instanceof PropertyExpression && !((PropertyExpression) call).isImplicitThis()) {
                final PropertyExpression property = (PropertyExpression) call;
                if (shouldWrap(property.getObjectExpression())) {
                    property.setObjectExpression(wrap(property.getObjectExpression(), line, column));
                }
            } else if (call instanceof MethodCallExpression && !((MethodCallExpression) call).isImplicitThis()) {
                final MethodCallExpression method = (MethodCallExpression) call;
                if (shouldWrap(method.getObjectExpression())) {
                    method.setObjectExpression(wrap(method.getObjectExpression(), line, column));
                }
            }
        }

        private static boolean isResponse(final BinaryExpression expression) {
            final int type = expression.getOperation().getType();
            return type == Types.RIGHT_SHIFT || type == Types.RIGHT_SHIFT_UNSIGNED;
        }

        /**
         * A target rooted at an implicit this, or at a variable inside a closure, can name a property of a closure
         * delegate. Spock resolves such names itself, so these targets stay as they are.
         */
        private boolean shouldWrap(final Expression target) {
            Expression root = target;
            while (root instanceof PropertyExpression || root instanceof MethodCallExpression) {
                if (isImplicitThis(root)) {
                    return false;
                }
                root = root instanceof PropertyExpression
                        ? ((PropertyExpression) root).getObjectExpression()
                        : ((MethodCallExpression) root).getObjectExpression();
            }
            if (root instanceof VariableExpression) {
                return closureDepth == 0 && !"_".equals(((VariableExpression) root).getName());
            }
            return true;
        }

        private static boolean isImplicitThis(final Expression expression) {
            return expression instanceof PropertyExpression
                    ? ((PropertyExpression) expression).isImplicitThis()
                    : ((MethodCallExpression) expression).isImplicitThis();
        }

        private static Expression wrap(final Expression target, final int line, final int column) {
            final MethodCallExpression begin = staticCall("beginWalk", VariableExpression.THIS_EXPRESSION,
                    new ConstantExpression(line), new ConstantExpression(column));
            final MethodCallExpression end = staticCall("endWalk", begin, target);
            begin.setSourcePosition(target);
            end.setSourcePosition(target);
            return end;
        }

        @Override
        public void visitMethodCallExpression(final MethodCallExpression call) {
            if (isDeepMockCall(call)) {
                rewrite(call);
            }
            super.visitMethodCallExpression(call);
        }

        private static boolean isDeepMockCall(final MethodCallExpression call) {
            final String name = call.getMethodAsString();
            final boolean deepMockName = "DeepMock".equals(name) || "GroovyDeepMock".equals(name);
            return deepMockName && (call.isImplicitThis() || isThis(call.getObjectExpression()));
        }

        private static boolean isThis(final Expression expression) {
            return expression instanceof VariableExpression && ((VariableExpression) expression).isThisExpression();
        }

        private static void rewrite(final MethodCallExpression call) {
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

        private static ClosureExpression trailingClosure(final List<Expression> arguments) {
            if (arguments.isEmpty()) {
                return null;
            }
            final Expression last = arguments.get(arguments.size() - 1);
            return last instanceof ClosureExpression ? (ClosureExpression) last : null;
        }

        private static List<Expression> leadingArguments(final List<Expression> arguments, final ClosureExpression closure) {
            return closure == null ? arguments : arguments.subList(0, arguments.size() - 1);
        }

        private static void rewriteLeading(final List<Expression> leading) {
            if (leading.isEmpty() || leading.get(0) instanceof ClassExpression) {
                leading.add(0, defaultResponseMap());
            } else if (leading.get(0) instanceof MapExpression) {
                addDefaultResponseEntry((MapExpression) leading.get(0));
            } else if (leading.size() == 1) {
                leading.set(0, new SpreadExpression(staticCall("arguments", leading.get(0))));
            } else {
                leading.set(0, staticCall("options", leading.get(0)));
            }
        }

        private static MapExpression defaultResponseMap() {
            final MapExpression map = new MapExpression();
            addDefaultResponseEntry(map);
            return map;
        }

        private static void addDefaultResponseEntry(final MapExpression map) {
            final boolean declared = map.getMapEntryExpressions().stream().anyMatch(Rewriter::isDefaultResponseEntry);
            if (!declared) {
                map.addMapEntryExpression(new ConstantExpression(DEFAULT_RESPONSE), supportField("RESPONSE"));
                return;
            }
            map.getMapEntryExpressions().stream()
                    .filter(Rewriter::isDefaultResponseEntry)
                    .forEach(entry -> entry.setValueExpression(staticCall("rejectDefaultResponse")));
        }

        private static boolean isDefaultResponseEntry(final MapEntryExpression entry) {
            return entry.getKeyExpression() instanceof ConstantExpression
                    && DEFAULT_RESPONSE.equals(((ConstantExpression) entry.getKeyExpression()).getValue());
        }

        private static PropertyExpression supportField(final String name) {
            return new PropertyExpression(supportClass(), name);
        }

        private static MethodCallExpression staticCall(final String method, final Expression... arguments) {
            final MethodCallExpression call = new MethodCallExpression(supportClass(), method, new ArgumentListExpression(arguments));
            call.setImplicitThis(false);
            return call;
        }

        private static ClassExpression supportClass() {
            return new ClassExpression(ClassHelper.make(DeepMockSupport.class));
        }
    }
}
