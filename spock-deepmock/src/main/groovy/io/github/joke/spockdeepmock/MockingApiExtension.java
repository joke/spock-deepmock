/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.joke.spockdeepmock;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.Singleton;
import groovy.transform.NamedParam;
import groovy.transform.NamedParams;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.SecondParam;
import groovy.transform.stc.SecondParam.FirstGenericType;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.util.Beta;
import spock.mock.MockingApi;

import java.util.List;
import java.util.Map;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Extends the {@link spock.mock.MockingApi} with Deep*Mock methods
 */
// copied and extended from spock.mock.MockingApi
public class MockingApiExtension {

    /**
     * Creates a deep mock whose type and name are inferred from the left-hand side of the enclosing variable assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   Person person = DeepMock() // type is Person.class, name is "person"
     * </pre>
     *
     * @return a deep mock whose type and name are inferred from the left-hand side of the enclosing variable assignment
     */
    public static <T> T DeepMock(final MockingApi self) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   Person person = DeepMock(name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the deep mock
     * @return a deep mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment
     */
    @Beta
    public static <T> T DeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class)}) final Map<String, Object> options) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified type. If enclosed in a variable assignment, the variable name will be
     * used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   def person = DeepMock(Person) // type is Person.class, name is "person"
     * </pre>
     *
     * @param type the interface or class type of the deep mock
     * @param <T>  the interface or class type of the deep mock
     * @return a deep mock with the specified type
     */
    public static <T> T DeepMock(final MockingApi self, final Class<T> type) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified options and type. If enclosed in an variable assignment, the variable name
     * will be used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   def person = DeepMock(Person, name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the deep mock
     * @param type    the interface or class type of the deep mock
     * @param <T>     the interface or class type of the deep mock
     * @return a deep mock with the specified options and type
     */
    @Beta
    public static <T> T DeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class)}) final Map<String, Object> options, final Class<T> type) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, expects one call to {@code sing()}
     *   Person person = DeepMock {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param interactions a description of the deep mock's interactions
     * @return a deep mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    public static <T> T DeepMock(final MockingApi self, final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, expects one call to {@code sing()}
     *   Person person = DeepMock(name: "myPerson") {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param options      optional options for creating the deep mock
     * @param interactions a description of the deep mock's interactions
     * @return a deep mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    public static <T> T DeepMock(final MockingApi self, final Map<String, Object> options, final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
     * used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   // name is "person", type is Person.class, returns hard-code value {@code name}, expects one call to {@code sing()}
     *   def person = DeepMock(Person) {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param type         the interface or class type of the deep mock
     * @param interactions a description of the deep mock's interactions
     * @param <T>          the interface or class type of the deep mock
     * @return a deep mock with the specified type and interactions
     */
    @Beta
    public static <T> T DeepMock(final MockingApi self, @DelegatesTo.Target final Class<T> type, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) @ClosureParams(FirstParam.FirstGenericType.class) final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a deep mock with the specified options, type, and interactions. If enclosed in a variable assignment, the
     * variable name will be used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, expects one call to {@code sing()}
     *   def person = DeepMock(Person, name: "myPerson") {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param options      options for creating the deep mock (see {@link org.spockframework.mock.IMockConfiguration} for available options})
     * @param type         the interface or class type of the deep mock
     * @param interactions a description of the deep mock's interactions
     * @param <T>          the interface or class type of the deep mock
     * @return a deep mock with the specified options, type, and interactions
     */
    @Beta
    public static <T> T DeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class)}) final Map<String, Object> options, @DelegatesTo.Target final Class<T> type, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) @ClosureParams(FirstGenericType.class) final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock whose type and name are inferred from the left-hand side of the enclosing variable assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   Person person = GroovyDeepMock() // type is Person.class, name is "person"
     * </pre>
     *
     * @return a Groovy deep mock whose type and name are inferred from the left-hand side of the enclosing variable assignment
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   Person person = GroovyDeepMock(name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the Groovy deep mock
     * @return a Groovy deep mock with the specified options whose type and name are inferred from the left-hand side of the
     * enclosing variable assignment
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class), @NamedParam(value = "global", type = Boolean.class)}) final Map<String, Object> options) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified type. If enclosed in a variable assignment, the variable name will be
     * used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   def person = GroovyDeepMock(Person) // type is Person.class, name is "person"
     * </pre>
     *
     * @param type the interface or class type of the Groovy deep mock
     * @param <T>  the interface or class type of the Groovy deep mock
     * @return a Groovy deep mock with the specified type
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, final Class<T> type) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified options and type. If enclosed in an variable assignment, the variable name
     * will be used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   def person = GroovyDeepMock(Person, name: "myPerson") // type is Person.class, name is "myPerson"
     * </pre>
     *
     * @param options optional options for creating the Groovy deep mock
     * @param type    the interface or class type of the Groovy deep mock
     * @param <T>     the interface or class type of the Groovy deep mock
     * @return a Groovy deep mock with the specified options and type
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class), @NamedParam(value = "global", type = Boolean.class)}) final Map<String, Object> options, final Class<T> type) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, expects one call to {@code sing()}
     *   Person person = GroovyMock {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param interactions a description of the Groovy deep mock's interactions
     * @return a Groovy deep mock with the specified interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment.
     * <p>
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, expects one call to {@code sing()}
     *   Person person = GroovyDeepMock(name: "myPerson") {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param options      optional options for creating the Groovy deep mock
     * @param interactions a description of the Groovy deep mock's interactions
     * @return a Groovy deep mock with the specified options and interactions whose type and name are inferred
     * from the left-hand side of the enclosing assignment
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class), @NamedParam(value = "global", type = Boolean.class)}) final Map<String, Object> options, final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
     * used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   // name is "person", type is Person.class, returns hard-code value {@code name}, expects one call to {@code sing()}
     *   def person = GroovyDeepMock(Person) {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param type         the interface or class type of the Groovy deep mock
     * @param interactions a description of the Groovy deep mock's interactions
     * @param <T>          the interface or class type of the Groovy deep mock
     * @return a Groovy deep mock with the specified type and interactions
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, @DelegatesTo.Target final Class<T> type, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) @ClosureParams(FirstParam.FirstGenericType.class) final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    /**
     * Creates a Groovy deep mock with the specified options, type, and interactions. If enclosed in a variable assignment, the
     * variable name will be used as the deep mock's name.
     * <p>
     * Example:
     *
     * <pre>
     *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, expects one call to {@code sing()}
     *   def person = GroovyDeepMock(Person, name: "myPerson") {
     *     name &gt;&gt; "Fred"
     *     1 * sing()
     *   }
     * </pre>
     *
     * @param options      options for creating the Groovy deep mock (see {@link org.spockframework.mock.IMockConfiguration} for available options})
     * @param type         the interface or class type of the deep mock
     * @param interactions a description of the Groovy deep mock's interactions
     * @param <T>          the interface or class type of the Groovy deep mock
     * @return a Groovy deep mock with the specified options, type, and interactions
     */
    @Beta
    public static <T> T GroovyDeepMock(final MockingApi self, @NamedParams({@NamedParam(value = "name", type = String.class), @NamedParam(value = "additionalInterfaces", type = List.class), @NamedParam(value = "verified", type = Boolean.class), @NamedParam(value = "useObjenesis", type = Boolean.class), @NamedParam(value = "global", type = Boolean.class)}) final Map<String, Object> options, @DelegatesTo.Target final Class<T> type, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) @ClosureParams(FirstGenericType.class) final Closure<?> interactions) {
        invalidMockCreation();
        return null;
    }

    private static void invalidMockCreation() {
        throw new InvalidSpecException("Mock objects can only be created inside a spec, and only during the lifetime of a feature (iteration)");
    }

}
