package io.github.joke.spockdeepmock

import java.lang.reflect.Modifier
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import spock.lang.Specification
import spock.lang.Tag

@Tag('unit')
class DeepMockTransformationSpec extends Specification {

    def "a DeepMock call is rewritten to a Mock call with the deep mock response"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    expect:
                    DeepMock(String) != null
                }
            }
        ''')

        expect:
        calledMethods(module).contains('Mock')
        calledMethods(module).contains('arguments')
        !calledMethods(module).contains('DeepMock')
    }

    def "a GroovyDeepMock call is rewritten to a GroovyMock call"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    expect:
                    GroovyDeepMock(String) != null
                }
            }
        ''')

        expect:
        calledMethods(module).contains('GroovyMock')
        !calledMethods(module).contains('GroovyDeepMock')
    }

    def "the target of an interaction in an instance method is wrapped in a walk"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    then:
                    1 * person.name()
                }
            }
        ''')

        expect:
        calledMethods(module).containsAll(['beginWalk', 'endWalk'])
    }

    def "an interaction in a static method is not wrapped"() {
        def module = rewrite('''
            class Sample {
                static def helper() {
                    1 * person.name()
                }
            }
        ''')

        expect:
        !calledMethods(module).contains('beginWalk')
    }

    def "an interaction on the wildcard is not wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    then:
                    1 * _.name()
                }
            }
        ''')

        expect:
        !calledMethods(module).contains('beginWalk')
    }

    def "an interaction on an implicit this is not wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    then:
                    1 * name()
                }
            }
        ''')

        expect:
        !calledMethods(module).contains('beginWalk')
    }

    def "a property target of an interaction is wrapped in a walk"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    then:
                    1 * person.name
                }
            }
        ''')

        expect:
        calledMethods(module).contains('beginWalk')
    }

    def "an implicit this property as the interaction call is not wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    then:
                    1 * name
                }
            }
        ''')

        expect:
        !calledMethods(module).contains('beginWalk')
    }

    def "a variable target inside a closure is not wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    given:
                    def check = { 1 * person.name(); 2 }
                }
            }
        ''')

        expect:
        !calledMethods(module).contains('beginWalk')
    }

    def "a target that is not a variable inside a closure is wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    given:
                    def check = { 1 * new Object().toString(); 2 }
                }
            }
        ''')

        expect:
        calledMethods(module).contains('beginWalk')
    }

    def "the closure depth is restored after a closure, so a later target is wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    given:
                    def check = { 1 }
                    then:
                    1 * person.name()
                }
            }
        ''')

        expect:
        calledMethods(module).contains('beginWalk')
    }

    def "an instance method after a static method is still rewritten"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                static def helper() {
                    1
                }

                def "feature"() {
                    then:
                    1 * person.name()
                }
            }
        ''')

        expect:
        calledMethods(module).contains('beginWalk')
    }

    def "an implicit this chain as the target of an interaction is not wrapped"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    then:
                    1 * name().child
                }
            }
        ''')

        expect:
        !calledMethods(module).contains('beginWalk')
    }

    def "a DeepMock call nested in the arguments of another call is rewritten"() {
        def module = rewrite('''
            class Sample extends spock.lang.Specification {
                def "feature"() {
                    given:
                    def mock = wrap(DeepMock(String))
                }
            }
        ''')

        expect:
        calledMethods(module).contains('Mock')
        !calledMethods(module).contains('DeepMock')
    }

    def "a property target with an implicit this is left alone, and an explicit one is wrapped"() {
        def rewriter = new DeepMockTransformation.Rewriter(emptySource())
        def implicit = new PropertyExpression(VariableExpression.THIS_EXPRESSION, 'name')
        implicit.implicitThis = true
        def explicit = new PropertyExpression(new VariableExpression('person'), 'name')
        explicit.implicitThis = false

        when:
        rewriter.wrapPropertyTarget(implicit, implicit)
        rewriter.wrapPropertyTarget(explicit, explicit)

        then:
        0 * _

        expect:
        implicit.objectExpression.is(VariableExpression.THIS_EXPRESSION)
        explicit.objectExpression instanceof MethodCallExpression
    }

    def "a static method restores the static flag of the code around it"() {
        def rewriter = new DeepMockTransformation.Rewriter(emptySource())
        def helper = new MethodNode('helper', Modifier.STATIC, ClassHelper.OBJECT_TYPE,
                [] as Parameter[], [] as ClassNode[], new BlockStatement())

        when:
        rewriter.visitConstructorOrMethod(helper, false)

        then:
        0 * _

        expect:
        !rewriter.@inStaticMethod
    }

    def "the rewriter reports the source unit it was given"() {
        def configuration = new CompilerConfiguration()
        def source = new SourceUnit('Sample.groovy', 'class Sample {}', configuration,
                new GroovyClassLoader(), new ErrorCollector(configuration))

        expect:
        new DeepMockTransformation.Rewriter(source).sourceUnit.is(source)
    }

    def "the walk records the line and column of the interaction"() {
        def module = rewrite('class Sample extends spock.lang.Specification {\n'
                + '    def "feature"() {\n'
                + '        then:\n'
                + '1 * person.name()\n'
                + '    }\n'
                + '}\n')

        expect:
        beginWalkPositions(module) == [[4, 1]]
    }

    private static SourceUnit emptySource() {
        def configuration = new CompilerConfiguration()
        new SourceUnit('Empty.groovy', 'class Empty {}', configuration, new GroovyClassLoader(),
                new ErrorCollector(configuration))
    }

    private static ModuleNode rewrite(String text) {
        def configuration = new CompilerConfiguration()
        def unit = new CompilationUnit(configuration)
        def source = unit.addSource('Sample.groovy', text)
        // The transformation is a global AST transform, so compiling to CONVERSION applies it once.
        unit.compile(Phases.CONVERSION)
        source.AST
    }

    private static List<List<Object>> beginWalkPositions(ModuleNode module) {
        def positions = []
        def visitor = new ClassCodeVisitorSupport() {
            final SourceUnit sourceUnit = null

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                if (call.methodAsString == 'beginWalk') {
                    positions << call.arguments.expressions[1..2]*.value
                }
                super.visitMethodCallExpression(call)
            }
        }
        module.classes.each { visitor.visitClass(it) }
        positions
    }

    private static Set<String> calledMethods(ModuleNode module) {
        def names = [] as Set<String>
        def visitor = new ClassCodeVisitorSupport() {
            final SourceUnit sourceUnit = null

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                names << call.methodAsString
                super.visitMethodCallExpression(call)
            }
        }
        module.classes.each { visitor.visitClass(it) }
        names
    }
}
