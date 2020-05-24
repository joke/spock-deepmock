package io.github.joke.spockdeepmock

import groovy.transform.CompileStatic
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SecondParam
import org.spockframework.mock.IDefaultResponse
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.util.Beta

import spock.mock.MockingApi

@CompileStatic
class MockingApiExtension {

    /**
     * Creates a mock whose type and name are inferred from the left-hand side of the enclosing variable assignment.
     *
     * Example:
     *
     * <pre>
     *   Person person = DeepMock() // type is Person.class, name is "person"
     * </pre>
     *
     * @return a mock whose type and name are inferred from the left-hand side of the enclosing variable assignment
     */
    static <T> T DeepMock(MockingApi self) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment.
     *
     * Example:
     *
     * <pre>
     *   Person person = DeepMock(name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the mock
     *
     * @return a mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment
     */
    @Beta
    static <T> T DeepMock(MockingApi self,
                          @NamedParams([
                                  @NamedParam(value = "name", type = String.class),
                                  @NamedParam(value = "additionalInterfaces", type = List.class),
                                  @NamedParam(value = "verified", type = Boolean.class),
                                  @NamedParam(value = "useObjenesis", type = Boolean.class)
                          ])
                                  Map<String, Object> options) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified type. If enclosed in a variable assignment, the variable name will be
     * used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   def person = DeepMock(Person) // type is Person.class, name is "person"
     * </pre>
     *
     * @param type the interface or class type of the mock
     * @param < T >  the interface or class type of the mock
     *
     * @return a mock with the specified type
     */
    static <T> T DeepMock(MockingApi self, Class<T> type) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified options and type. If enclosed in an variable assignment, the variable name
     * will be used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   def person = DeepMock(Person, name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the mock
     * @param type the interface or class type of the mock
     * @param < T >  the interface or class type of the mock
     *
     * @return a mock with the specified options and type
     */
    @Beta
    static <T> T DeepMock(MockingApi self,
                          @NamedParams([
                                  @NamedParam(value = "name", type = String.class),
                                  @NamedParam(value = "additionalInterfaces", type = List.class),
                                  @NamedParam(value = "verified", type = Boolean.class),
                                  @NamedParam(value = "useObjenesis", type = Boolean.class)
                          ])
                                  Map<String, Object> options, Class<T> type) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     *
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, expects one call to {@code sing( )}
     *   Person person = Mock {*     it.name >> "Fred"
     *     1 * sing()
     *}* </pre>
     *
     * @param interactions a description of the mock's interactions
     *
     * @return a mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    static <T> T DeepMock(MockingApi self, Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     *
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, expects one call to {@code sing( )}
     *   Person person = DeepMock(name: "myPerson") {*     it.name >> "Fred"
     *     1 * sing()
     *}* </pre>
     *
     * @param options optional options for creating the mock
     * @param interactions a description of the mock's interactions
     *
     * @return a mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    static <T> T DeepMock(MockingApi self, Map<String, Object> options, Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
     * used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   // name is "person", type is Person.class, returns hard-code value {@code name}, expects one call to {@code sing( )}
     *   def person = DeepMock(Person) {*     it.name >> "Fred"
     *     1 * sing()
     *}* </pre>
     *
     * @param type the interface or class type of the mock
     * @param interactions a description of the mock's interactions
     * @param < T >  the interface or class type of the mock
     *
     * @return a mock with the specified type and interactions
     */
    @Beta
    static <T> T DeepMock(MockingApi self,
                          @DelegatesTo.Target
                                  Class<T> type,
                          @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
                          @ClosureParams(FirstParam.FirstGenericType.class)
                                  Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a mock with the specified options, type, and interactions. If enclosed in a variable assignment, the
     * variable name will be used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, expects one call to {@code sing( )}
     *   def person = DeepMock(Person, name: "myPerson") {*     it.name >> "Fred"
     *     1 * sing()
     *}* </pre>
     *
     * @param options options for creating the mock (see {@link org.spockframework.mock.IMockConfiguration} for available options})
     * @param type the interface or class type of the mock
     * @param interactions a description of the mock's interactions
     * @param < T >  the interface or class type of the mock
     *
     * @return a mock with the specified options, type, and interactions
     */
    @Beta
    static <T> T DeepMock(MockingApi self,
                          @NamedParams([
                                  @NamedParam(value = "name", type = String.class),
                                  @NamedParam(value = "additionalInterfaces", type = List.class),
                                  @NamedParam(value = "verified", type = Boolean.class),
                                  @NamedParam(value = "useObjenesis", type = Boolean.class)
                          ])
                                  Map<String, Object> options,
                          @DelegatesTo.Target
                                  Class<T> type,
                          @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
                          @ClosureParams(SecondParam.FirstGenericType.class)
                                  Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock whose type and name are inferred from the left-hand side of the enclosing variable assignment.
     *
     * Example:
     *
     * <pre>
     *   Person person = GroovyDeepMock() // type is Person.class, name is "person"
     * </pre>
     *
     * @return a Groovy mock whose type and name are inferred from the left-hand side of the enclosing variable assignment
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment.
     *
     * Example:
     *
     * <pre>
     *   Person person = GroovyDeepMock(name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the Groovy mock
     *
     * @return a Groovy mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self,
            @NamedParams([
                @NamedParam(value = "name", type = String.class),
                @NamedParam(value = "additionalInterfaces", type = List.class),
                @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
                @NamedParam(value = "verified", type = Boolean.class),
                @NamedParam(value = "useObjenesis", type = Boolean.class),
                @NamedParam(value = "global", type = Boolean.class)
            ])
                    Map<String, Object> options) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified type. If enclosed in a variable assignment, the variable name will be
     * used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   def person = GroovyDeepMock(Person) // type is Person.class, name is "person"
     * </pre>
     *
     * @param type the interface or class type of the Groovy mock
     * @param <T> the interface or class type of the Groovy mock
     *
     * @return a Groovy mock with the specified type
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self, Class<T> type) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified options and type. If enclosed in an variable assignment, the variable name
     * will be used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   def person = GroovyDeepMock(Person, name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the Groovy mock
     * @param type the interface or class type of the Groovy mock
     * @param <T> the interface or class type of the Groovy mock
     *
     * @return a Groovy mock with the specified options and type
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self,
            @NamedParams([
                @NamedParam(value = "name", type = String.class),
                @NamedParam(value = "additionalInterfaces", type = List.class),
                @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
                @NamedParam(value = "verified", type = Boolean.class),
                @NamedParam(value = "useObjenesis", type = Boolean.class),
                @NamedParam(value = "global", type = Boolean.class)
            ])
                    Map<String, Object> options, Class<T> type) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     *
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, expects one call to {@code sing()}
     *   Person person = GroovyDeepMock {
     *     it.name >> "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param interactions a description of the Groovy mock's interactions
     *
     * @return a Groovy mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self, Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     *
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, expects one call to {@code sing()}
     *   Person person = GroovyDeepMock(name: "myPerson") {
     *     it.name >> "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param options optional options for creating the Groovy mock
     * @param interactions a description of the Groovy mock's interactions
     *
     * @return a Groovy mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self,
            @NamedParams([
                @NamedParam(value = "name", type = String.class),
                @NamedParam(value = "additionalInterfaces", type = List.class),
                @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
                @NamedParam(value = "verified", type = Boolean.class),
                @NamedParam(value = "useObjenesis", type = Boolean.class),
                @NamedParam(value = "global", type = Boolean.class)
            ])
                    Map<String, Object> options, Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
     * used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   // name is "person", type is Person.class, returns hard-code value {@code name}, expects one call to {@code sing()}
     *   def person = GroovyDeepMock(Person) {
     *     it.name >> "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param type the interface or class type of the Groovy mock
     * @param interactions a description of the Groovy mock's interactions
     * @param <T> the interface or class type of the Groovy mock
     *
     * @return a Groovy mock with the specified type and interactions
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self,
            @DelegatesTo.Target
                    Class<T> type,
            @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
            @ClosureParams(FirstParam.FirstGenericType.class)
                    Closure interactions) {
        invalidMockCreation()
        return null
    }

    /**
     * Creates a Groovy mock with the specified options, type, and interactions. If enclosed in a variable assignment, the
     * variable name will be used as the mock's name.
     *
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, expects one call to {@code sing()}
     *   def person = GroovyDeepMock(Person, name: "myPerson") {
     *     it.name >> "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param options options for creating the Groovy mock (see {@link org.spockframework.mock.IMockConfiguration} for available options])
     * @param type the interface or class type of the mock
     * @param interactions a description of the Groovy mock's interactions
     * @param <T> the interface or class type of the Groovy mock
     *
     * @return a Groovy mock with the specified options, type, and interactions
     */
    @Beta
    static <T> T GroovyDeepMock(MockingApi self,
            @NamedParams([
                @NamedParam(value = "name", type = String.class),
                @NamedParam(value = "additionalInterfaces", type = List.class),
                @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
                @NamedParam(value = "verified", type = Boolean.class),
                @NamedParam(value = "useObjenesis", type = Boolean.class),
                @NamedParam(value = "global", type = Boolean.class)
            ])
                    Map<String, Object> options,
            @DelegatesTo.Target
                    Class<T> type,
            @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
            @ClosureParams(SecondParam.FirstGenericType.class)
                    Closure interactions) {
        invalidMockCreation()
        return null
    }

    private static void invalidMockCreation() {
        throw new InvalidSpecException("Mock objects can only be created inside a spec, and only during the lifetime of a feature (iteration)")
    }
}
