package io.github.joke.spockdeepmock;

import static org.codehaus.groovy.control.CompilePhase.CONVERSION;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.jetbrains.annotations.VisibleForTesting;

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

    // The overrides below must be public, because Groovy's visitor declares them public.
    private static final class Rewriter extends ClassCodeVisitorSupport {

        private final SourceUnit source;

        private boolean inStaticMethod;

        private int closureDepth;

        Rewriter(final SourceUnit source) {
            super();
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

        @Override
        public void visitMethodCallExpression(final MethodCallExpression call) {
            if (DeepMockAst.isDeepMockCall(call)) {
                DeepMockAst.rewrite(call);
            }
            super.visitMethodCallExpression(call);
        }

        @VisibleForTesting
        void wrapInteractionTarget(final ExpressionStatement statement) {
            final Expression expression = statement.getExpression();
            final Expression call = DeepMockAst.interactionCall(expression);
            if (call instanceof PropertyExpression) {
                wrapPropertyTarget((PropertyExpression) call, expression);
            } else if (call instanceof MethodCallExpression) {
                wrapMethodTarget((MethodCallExpression) call, expression);
            }
        }

        @VisibleForTesting
        void wrapPropertyTarget(final PropertyExpression property, final Expression expression) {
            if (!property.isImplicitThis() && shouldWrap(property.getObjectExpression())) {
                property.setObjectExpression(wrapTarget(property.getObjectExpression(), expression));
            }
        }

        @VisibleForTesting
        void wrapMethodTarget(final MethodCallExpression method, final Expression expression) {
            if (!method.isImplicitThis() && shouldWrap(method.getObjectExpression())) {
                method.setObjectExpression(wrapTarget(method.getObjectExpression(), expression));
            }
        }

        @VisibleForTesting
        Expression wrapTarget(final Expression target, final Expression expression) {
            return DeepMockAst.wrap(target, expression.getLineNumber(), expression.getColumnNumber());
        }

        /**
         * A target rooted at an implicit this, or at a variable inside a closure, can name a property of a closure
         * delegate. Spock resolves such names itself, so these targets stay as they are.
         */
        @VisibleForTesting
        boolean shouldWrap(final Expression target) {
            final Expression root = DeepMockAst.rootOf(target);
            if (root == null) {
                return false;
            }
            if (root instanceof VariableExpression) {
                return closureDepth == 0 && !"_".equals(((VariableExpression) root).getName());
            }
            return true;
        }
    }
}
