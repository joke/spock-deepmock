package io.github.joke.spockdeepmock;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.spockframework.util.Identifiers;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.codehaus.groovy.control.CompilePhase.INITIALIZATION;
import static org.spockframework.runtime.GroovyRuntimeUtil.getProperty;

@GroovyASTTransformation(phase = INITIALIZATION)
public class SpockTransformationInitialization implements ASTTransformation {

    private static final List<String> DEEP_MOCK_IDENTIFIERS = asList("DeepMock", "GroovyDeepMock");

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        accessIdentifierProperty("BUILT_IN_METHODS").addAll(DEEP_MOCK_IDENTIFIERS);
        accessIdentifierProperty("TEST_DOUBLE_METHODS").addAll(DEEP_MOCK_IDENTIFIERS);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> accessIdentifierProperty(final String property) {
        return (Set<String>) getProperty(Identifiers.class, property);
    }

}
