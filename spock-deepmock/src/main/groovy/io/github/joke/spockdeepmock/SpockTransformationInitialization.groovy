package io.github.joke.spockdeepmock

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.control.CompilePhase.INITIALIZATION
import static org.spockframework.util.Identifiers.BUILT_IN_METHODS
import static org.spockframework.util.Identifiers.TEST_DOUBLE_METHODS

@CompileStatic
@GroovyASTTransformation(phase = INITIALIZATION)
class SpockTransformationInitialization implements ASTTransformation {

    static List DEEP_MOCK_IDENTIFIERS = ['DeepMock', 'GroovyDeepMock']

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        BUILT_IN_METHODS.addAll(DEEP_MOCK_IDENTIFIERS)
        TEST_DOUBLE_METHODS.addAll(DEEP_MOCK_IDENTIFIERS)
    }

}
