package io.github.joke.spockdeepmock

import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import java.lang.reflect.Modifier
import spock.lang.Specification
import spock.lang.Tag

@Tag('unit')
class DeepMockAstSpec extends Specification {

    static VariableExpression var(String name) {
        new VariableExpression(name)
    }

    static MethodCallExpression call(Expression object, String name, Expression... arguments) {
        def call = new MethodCallExpression(object, name, new ArgumentListExpression(arguments))
        call.implicitThis = false
        call
    }

    static MethodCallExpression implicitCall(String name, Expression... arguments) {
        def call = call(THIS_EXPRESSION, name, arguments)
        call.implicitThis = true
        call
    }

    static PropertyExpression prop(Expression object, String name) {
        def property = new PropertyExpression(object, name)
        property.implicitThis = false
        property
    }

    static BinaryExpression bin(Expression left, int type, Expression right) {
        new BinaryExpression(left, Token.newSymbol(type, 1, 1), right)
    }

    static BinaryExpression multiply(Expression left, Expression right) {
        bin(left, Types.MULTIPLY, right)
    }

    static BinaryExpression response(Expression left, Expression right) {
        bin(left, Types.RIGHT_SHIFT, right)
    }

    static MapEntryExpression entry(String key, Expression value) {
        new MapEntryExpression(new ConstantExpression(key), value)
    }

    def "a shift is a response, an unsigned shift too, and a multiplication is not"() {
        expect:
        DeepMockAst.isResponse(bin(var('a'), type, var('b'))) == expected

        where:
        type                       | expected
        Types.RIGHT_SHIFT          | true
        Types.RIGHT_SHIFT_UNSIGNED | true
        Types.MULTIPLY             | false
    }

    def "stripping responses removes every response from the outside in"() {
        def chain = call(var('mock'), 'name')

        expect:
        DeepMockAst.stripResponses(chain).is(chain)
        DeepMockAst.stripResponses(response(chain, var('r'))).is(chain)
        DeepMockAst.stripResponses(response(response(chain, var('r')), var('s'))).is(chain)
        DeepMockAst.stripResponses(multiply(var('n'), chain)) instanceof BinaryExpression
    }

    def "an interaction call is the right side of a multiplication, with or without a response"() {
        def target = call(var('mock'), 'name')

        expect:
        DeepMockAst.interactionCall(multiply(var('n'), target)).is(target)
        DeepMockAst.interactionCall(response(multiply(var('n'), target), var('r'))).is(target)
    }

    def "a response without a multiplication declares its call with no cardinality"() {
        def target = call(var('mock'), 'name')

        expect:
        DeepMockAst.interactionCall(response(target, var('r'))).is(target)
    }

    def "a plain expression is not an interaction call"() {
        expect:
        DeepMockAst.interactionCall(call(var('mock'), 'name')) == null
        DeepMockAst.interactionCall(var('mock')) == null
    }

    def "a response is detected only on a shift expression"() {
        expect:
        DeepMockAst.hasResponse(response(var('a'), var('b')))
        !DeepMockAst.hasResponse(multiply(var('a'), var('b')))
        !DeepMockAst.hasResponse(var('a'))
    }

    def "a multiplication is detected only on a multiply expression"() {
        expect:
        DeepMockAst.isMultiply(multiply(var('a'), var('b')))
        !DeepMockAst.isMultiply(bin(var('a'), Types.PLUS, var('b')))
        !DeepMockAst.isMultiply(var('a'))
    }

    def "the root of a chain is the variable it starts from"() {
        expect:
        DeepMockAst.rootOf(call(prop(var('mock'), 'child'), 'name')) instanceof VariableExpression
        DeepMockAst.rootOf(call(prop(var('mock'), 'child'), 'name')).name == 'mock'
        DeepMockAst.rootOf(var('mock')).name == 'mock'
    }

    def "the root of a chain starting at an implicit this is null"() {
        expect:
        DeepMockAst.rootOf(prop(implicitProperty('name'), 'child')) == null
        DeepMockAst.rootOf(implicitCall('name')) == null
    }

    def "a chain is a property or a method call"() {
        expect:
        DeepMockAst.isChain(prop(var('a'), 'b'))
        DeepMockAst.isChain(call(var('a'), 'b'))
        !DeepMockAst.isChain(var('a'))
    }

    def "implicit this is read from a property or a method call"() {
        expect:
        DeepMockAst.isImplicitThis(implicitProperty('a'))
        !DeepMockAst.isImplicitThis(prop(var('a'), 'b'))
        DeepMockAst.isImplicitThis(implicitCall('a'))
        !DeepMockAst.isImplicitThis(call(var('a'), 'b'))
    }

    def "the object of a property or a method call is its receiver"() {
        def receiver = var('mock')

        expect:
        DeepMockAst.objectOf(prop(receiver, 'child')).is(receiver)
        DeepMockAst.objectOf(call(receiver, 'child')).is(receiver)
    }

    def "wrapping a target gives both calls the source position of the target"() {
        def target = var('mock')
        target.lineNumber = 7
        target.columnNumber = 9

        when:
        def wrapped = DeepMockAst.wrap(target, 12, 34)
        def begin = wrapped.arguments.expressions[0]

        then:
        0 * _

        expect:
        wrapped.methodAsString == 'endWalk'
        wrapped.arguments.expressions[1].is(target)
        begin.methodAsString == 'beginWalk'
        begin.arguments.expressions[0].is(THIS_EXPRESSION)
        begin.arguments.expressions[1].value == 12
        begin.arguments.expressions[2].value == 34
        wrapped.lineNumber == 7
        wrapped.columnNumber == 9
        begin.lineNumber == 7
        begin.columnNumber == 9
    }

    def "a DeepMock call is one with an implicit this or a this receiver"() {
        expect:
        DeepMockAst.isDeepMockCall(implicitCall(name))
        DeepMockAst.isDeepMockCall(call(THIS_EXPRESSION, name))
        !DeepMockAst.isDeepMockCall(call(var('other'), name))
        !DeepMockAst.isDeepMockCall(call(THIS_EXPRESSION, 'Other'))

        where:
        name << ['DeepMock', 'GroovyDeepMock']
    }

    def "an implicit this call is a DeepMock call whatever its receiver node"() {
        def mock = implicitCall('DeepMock')
        mock.objectExpression = var('other')

        expect:
        DeepMockAst.isDeepMockCall(mock)
    }

    def "a DeepMock name is DeepMock or GroovyDeepMock only"() {
        expect:
        DeepMockAst.isDeepMockName('DeepMock')
        DeepMockAst.isDeepMockName('GroovyDeepMock')
        !DeepMockAst.isDeepMockName('Mock')
        !DeepMockAst.isDeepMockName('deepmock')
    }

    def "this is detected only on the this variable"() {
        expect:
        DeepMockAst.isThis(THIS_EXPRESSION)
        !DeepMockAst.isThis(var('me'))
        !DeepMockAst.isThis(ConstantExpression.NULL)
    }

    def "a DeepMock call is rewritten to Mock, and a GroovyDeepMock call to GroovyMock"() {
        def mock = implicitCall('DeepMock', new ClassExpression(ClassHelper.make(String)))
        def groovy = implicitCall('GroovyDeepMock', new ClassExpression(ClassHelper.make(String)))

        when:
        DeepMockAst.rewrite(mock)
        DeepMockAst.rewrite(groovy)

        then:
        0 * _

        expect:
        mock.methodAsString == 'Mock'
        groovy.methodAsString == 'GroovyMock'
    }

    def "a class argument gets the default response map in front of it"() {
        def type = new ClassExpression(ClassHelper.make(String))
        def mock = implicitCall('DeepMock', type)

        when:
        DeepMockAst.rewrite(mock)

        then:
        0 * _

        expect:
        mock.arguments.expressions.size() == 2
        mock.arguments.expressions[0] instanceof MapExpression
        mock.arguments.expressions[0].mapEntryExpressions[0].keyExpression.value == 'defaultResponse'
        mock.arguments.expressions[1].is(type)
    }

    def "a trailing closure stays last and is not treated as a leading argument"() {
        def type = new ClassExpression(ClassHelper.make(String))
        def closure = new ClosureExpression(null, null)
        def mock = implicitCall('DeepMock', type, closure)

        when:
        DeepMockAst.rewrite(mock)

        then:
        0 * _

        expect:
        mock.arguments.expressions.size() == 3
        mock.arguments.expressions[1].is(type)
        mock.arguments.expressions[2].is(closure)
    }

    def "an options map gets the default response entry"() {
        def options = new MapExpression([entry('name', new ConstantExpression('x'))])
        def mock = implicitCall('DeepMock', options)

        when:
        DeepMockAst.rewrite(mock)

        then:
        0 * _

        expect:
        mock.arguments.expressions[0].mapEntryExpressions.size() == 2
        mock.arguments.expressions[0].mapEntryExpressions[1].keyExpression.value == 'defaultResponse'
    }

    def "a closure without a class is the only argument, and the response map comes in front of it"() {
        def block = closure()
        def mock = implicitCall('DeepMock', block)

        when:
        DeepMockAst.rewrite(mock)

        then:
        0 * _

        expect:
        mock.arguments.expressions.size() == 2
        mock.arguments.expressions[0] instanceof MapExpression
        mock.arguments.expressions[1].is(block)
    }

    def "a single non-map argument is spread as the arguments of a call"() {
        def mock = implicitCall('DeepMock', var('value'))

        when:
        DeepMockAst.rewrite(mock)

        then:
        0 * _

        expect:
        mock.arguments.expressions[0] instanceof SpreadExpression
    }

    def "a leading class without a closure leaves the trailing arguments alone"() {
        expect:
        DeepMockAst.trailingClosure([]) == null
        DeepMockAst.trailingClosure([var('a')]) == null
        DeepMockAst.trailingClosure([var('a'), closure()]) instanceof ClosureExpression
    }

    def "the leading arguments drop the trailing closure only"() {
        def arguments = [var('a'), var('b'), closure()]

        expect:
        DeepMockAst.leadingArguments(arguments, null).is(arguments)
        DeepMockAst.leadingArguments(arguments, arguments[2]) == [arguments[0], arguments[1]]
    }

    def "a leading class or nothing gets the default response map"() {
        def leading = leadingOf(arguments)

        when:
        DeepMockAst.rewriteLeading(leading)

        then:
        0 * _

        expect:
        leading[0] instanceof MapExpression
        leading.size() == expectedSize

        where:
        arguments                                       | expectedSize
        []                                              | 1
        [new ClassExpression(ClassHelper.make(String))] | 2
    }

    def "a map gets the default response entry and keeps its other entries"() {
        def map = new MapExpression([entry('name', var('n'))])
        def leading = [map]

        when:
        DeepMockAst.rewriteLeading(leading)

        then:
        0 * _

        expect:
        leading.size() == 1
        leading[0].is(map)
        map.mapEntryExpressions*.keyExpression*.value == ['name', 'defaultResponse']
    }

    def "any other first argument is replaced by its passed form"() {
        def first = var('a')
        def second = var('b')
        def leading = [first, second]

        when:
        DeepMockAst.rewriteLeading(leading)

        then:
        0 * _

        expect:
        leading.size() == 2
        leading[0] instanceof MethodCallExpression
        leading[0].methodAsString == 'options'
        leading[0].arguments.expressions[0].is(first)
        leading[1].is(second)
    }

    def "a single argument is passed as spread arguments and several as options"() {
        def first = var('a')
        def second = var('b')

        expect:
        DeepMockAst.passedArgument([first]) instanceof SpreadExpression
        DeepMockAst.passedArgument([first, second]) instanceof MethodCallExpression
        DeepMockAst.passedArgument([first, second]).methodAsString == 'options'
        DeepMockAst.passedArgument([first, second]).arguments.expressions[0].is(first)
    }

    def "the default response map holds the deep mock response"() {
        when:
        def map = DeepMockAst.defaultResponseMap()

        then:
        0 * _

        expect:
        map.mapEntryExpressions.size() == 1
        map.mapEntryExpressions[0].keyExpression.value == 'defaultResponse'
        map.mapEntryExpressions[0].valueExpression instanceof PropertyExpression
        map.mapEntryExpressions[0].valueExpression.propertyAsString == 'RESPONSE'
    }

    def "an absent default response entry is added with the deep mock response"() {
        def map = new MapExpression([entry('name', var('n'))])

        when:
        DeepMockAst.addDefaultResponseEntry(map)

        then:
        0 * _

        expect:
        map.mapEntryExpressions.size() == 2
        map.mapEntryExpressions[1].valueExpression.propertyAsString == 'RESPONSE'
    }

    def "a declared default response entry is replaced by a rejection and not duplicated"() {
        def map = new MapExpression([entry('defaultResponse', var('mine')), entry('name', var('n'))])

        when:
        DeepMockAst.addDefaultResponseEntry(map)

        then:
        0 * _

        expect:
        map.mapEntryExpressions.size() == 2
        map.mapEntryExpressions[0].valueExpression.methodAsString == 'rejectDefaultResponse'
        map.mapEntryExpressions[0].valueExpression.arguments.expressions.empty
        map.mapEntryExpressions[1].keyExpression.value == 'name'
        map.mapEntryExpressions[1].valueExpression.name == 'n'
    }

    def "a default response entry is recognised by its constant key only"() {
        expect:
        DeepMockAst.isDefaultResponseEntry(entry('defaultResponse', var('v')))
        !DeepMockAst.isDefaultResponseEntry(entry('other', var('v')))
        !DeepMockAst.isDefaultResponseEntry(new MapEntryExpression(var('defaultResponse'), var('v')))
    }

    def "a support field names a field of the support class"() {
        when:
        def field = DeepMockAst.supportField('RESPONSE')

        then:
        0 * _

        expect:
        field.propertyAsString == 'RESPONSE'
        field.objectExpression.type.name == DeepMockSupport.name
    }

    def "a static call targets the support class without an implicit this"() {
        when:
        def call = DeepMockAst.staticCall('options', var('a'))

        then:
        0 * _

        expect:
        call.methodAsString == 'options'
        !call.implicitThis
        call.objectExpression.type.name == DeepMockSupport.name
        call.arguments.expressions.size() == 1
    }

    def "the utility class has a private constructor"() {
        def constructor = DeepMockAst.declaredConstructors[0]
        constructor.accessible = true

        expect:
        Modifier.isPrivate(constructor.modifiers)
        constructor.newInstance() instanceof DeepMockAst
    }

    def "the support class expression names the support class"() {
        expect:
        DeepMockAst.supportClass().type.name == DeepMockSupport.name
    }

    private static Expression implicitProperty(String name) {
        def property = prop(THIS_EXPRESSION, name)
        property.implicitThis = true
        property
    }

    private static ClosureExpression closure() {
        new ClosureExpression(null, null)
    }

    private static List<Expression> leadingOf(List<Expression> arguments) {
        new ArrayList<Expression>(arguments)
    }
}
