# deep-mocking Specification

## Purpose
Deep mocks let a Spock specification stub and verify calls at the end of a call chain, such as `nested.child.child.name`, without declaring a mock for every object in the chain. This capability covers how deep mocks are created, which mock implementation they use, when two calls share a nested mock, and how long nested mocks live.

## Requirements
### Requirement: Deep mock creation
The library SHALL provide `DeepMock()` and `GroovyDeepMock()` in every Spock `Specification`, with the same overloads as Spock's `Mock()` and `GroovyMock()`: inferred type and name, options map, explicit type, interaction closure, and their combinations. A deep mock SHALL answer every call that no interaction matches with a nested deep mock of the method's return type when the mock makers can mock that type. When the mock makers cannot mock the return type, the deep mock SHALL answer the call like a Spock `Mock()`: `null` for `void` and for reference types, and the default value for primitive types. Only the mock makers SHALL decide if a type can be mocked.

#### Scenario: Type and name inferred from the assignment
- **WHEN** a specification declares `Nested nested = DeepMock()`
- **THEN** `nested` is a mock of type `Nested` named `nested`

#### Scenario: Name from the options
- **WHEN** a specification declares `Nested nested = DeepMock(name: 'customName')`
- **THEN** `nested` is a mock named `customName`

#### Scenario: Unstubbed call returns a nested deep mock
- **WHEN** a call on a deep mock is not matched by any interaction and the mock makers can mock its return type
- **THEN** the call returns a deep mock of that return type

#### Scenario: Unstubbed void call
- **WHEN** a feature calls an unstubbed `void` method on a deep mock
- **THEN** the call returns normally

#### Scenario: Unstubbed call with a primitive return type
- **WHEN** a feature calls unstubbed methods on a deep mock that return `int` and `boolean`
- **THEN** the calls return `0` and `false`

#### Scenario: Unstubbed call with a reference type that cannot be mocked
- **WHEN** a feature calls unstubbed methods on a deep mock that return `java.lang.String` and `java.lang.Integer`
- **THEN** both calls return `null`

#### Scenario: Final class that a mock maker can mock
- **WHEN** the `mockito` mock maker is on the test classpath and a feature calls an unstubbed method on a deep mock that returns the final class `FinalLeaf`
- **THEN** the call returns a deep mock of type `FinalLeaf`

#### Scenario: Loop over an unstubbed collection
- **WHEN** code under test iterates over the `List` that an unstubbed call on a deep mock returns
- **THEN** the loop body runs zero times

#### Scenario: Stubbed call at the end of a chain
- **WHEN** a specification stubs `nested.child.child.name >> 'third'`
- **THEN** `nested.child.child.name` returns `'third'`

### Requirement: Mock implementation
`DeepMock()` SHALL create a Java mock. `GroovyDeepMock()` SHALL create a Groovy mock. A nested deep mock SHALL be a Groovy mock when the method's return type implements `groovy.lang.GroovyObject`, and a Java mock otherwise.

#### Scenario: Top-level DeepMock is a Java mock
- **WHEN** a specification declares `Nested nested = DeepMock()`
- **THEN** the mock implementation of `nested` is `JAVA`

#### Scenario: Top-level GroovyDeepMock is a Groovy mock
- **WHEN** a specification declares `GroovyNested nested = GroovyDeepMock()`
- **THEN** the mock implementation of `nested` is `GROOVY`

#### Scenario: Nested mock of a Groovy class
- **WHEN** an unstubbed call on a deep mock returns the Groovy class `GroovyNested`
- **THEN** the returned nested mock has the mock implementation `GROOVY`

#### Scenario: Nested mock of a Java class
- **WHEN** an unstubbed call on a deep mock returns the Java class `Nested`
- **THEN** the returned nested mock has the mock implementation `JAVA`

### Requirement: Options handling
The library SHALL reject a `defaultResponse` option, because deep mocks supply their own default response. The library SHALL NOT modify the options map that the caller passes in.

#### Scenario: Default response option is rejected
- **WHEN** a specification declares `Nested nested = DeepMock(defaultResponse: ZeroOrNullResponse.INSTANCE)`
- **THEN** the mock creation fails with an `UnsupportedOperationException`

#### Scenario: Caller options are not modified
- **WHEN** a specification passes the map `options = [name: 'customName']` to `DeepMock(options)`
- **THEN** `options` still contains only the key `name` after the mock is created

### Requirement: Nested mock identity per call
Within one specification instance, calls on the same deep mock with the same method (name and parameter types) and equal arguments SHALL return the same nested mock, including across interaction scopes such as consecutive `then:` blocks. Calls with a different method name, different parameter types, or arguments that are not equal SHALL return different nested mocks. Argument equality SHALL be the equality that Spock uses for equal-argument constraints.

#### Scenario: Repeated property access
- **WHEN** a feature reads `nested.child` twice
- **THEN** both reads return the same nested mock

#### Scenario: Saved nested mock
- **WHEN** a feature saves `def second = nested.child` and later code calls `nested.child`
- **THEN** that call returns `second`

#### Scenario: Different methods with the same return type
- **WHEN** a feature calls `nested.child` and `nested.child('x')`, which both return `Nested`
- **THEN** the two calls return different nested mocks

#### Scenario: Different arguments
- **WHEN** a feature calls `nested.child('first')` and `nested.child('second')`
- **THEN** the two calls return different nested mocks

#### Scenario: Equal arguments
- **WHEN** a feature calls `nested.child('first')` twice with equal arguments
- **THEN** both calls return the same nested mock

#### Scenario: Overloads with equal arguments
- **WHEN** a feature calls the overloads `nested.lookup(String)` and `nested.lookup(Object)` with the same argument `'x'`
- **THEN** the two calls return different nested mocks, each of its own return type

### Requirement: Strict mocking with nested chains
A chain of deep mock calls in an interaction SHALL register the intermediate calls, so that code under test can make the same intermediate calls without matching a catch-all interaction such as `0 * _`.

#### Scenario: Verified chain with catch-all
- **WHEN** a feature declares `1 * nested.child.child.name >> 'Hello'` and `0 * _`, and the code under test calls `nested.getChild().getChild().getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

#### Scenario: Verified chain with arguments
- **WHEN** a feature declares `1 * nested.child('first').child('second').name >> 'Hello'` and `0 * _`, and the code under test calls `nested.child("first").child("second").getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

### Requirement: Nested mock lifetime
The library SHALL keep nested deep mocks only for the lifetime of the specification instance whose deep mock created them. Nested deep mocks SHALL NOT be shared between specification instances, such as two features or two iterations of a data-driven feature.

#### Scenario: Separate features
- **WHEN** two features of one specification each call `nested.child` on their own deep mock
- **THEN** each feature gets its own nested mock

#### Scenario: Specification instance ends
- **WHEN** a specification instance completes
- **THEN** the library holds no reference to the nested deep mocks that the instance created

### Requirement: Concurrent calls
Calls on deep mocks from several threads within one specification instance SHALL keep the nested mock identity per call.

#### Scenario: Same call from several threads
- **WHEN** several threads call `nested.child` on the same deep mock at the same time
- **THEN** every thread gets the same nested mock

### Requirement: Object methods
Calls of the `java.lang.Object` methods `equals`, `hashCode` and `toString` on a deep mock that no interaction matches SHALL be answered by Spock's default `Object` behavior and SHALL NOT return nested deep mocks.

#### Scenario: toString of a deep mock
- **WHEN** a feature calls `toString()` on an unstubbed deep mock
- **THEN** the call returns Spock's default mock description, not a mock

### Requirement: Nested mock options
A nested deep mock SHALL use the `mockMaker`, `verified` and `useObjenesis` settings of the deep mock that created it. A nested deep mock SHALL NOT take the `name`, `additionalInterfaces`, `global` and `constructorArgs` options of its parent.

#### Scenario: Mock maker of the parent
- **WHEN** the `mockito` mock maker is on the test classpath, a specification declares `Nested nested = DeepMock(mockMaker: MockMakers.byteBuddy)`, and a feature calls an unstubbed method on `nested` that returns the final class `FinalLeaf`
- **THEN** the call returns `null`, because the `byte-buddy` mock maker cannot mock final classes

#### Scenario: Verified setting of the parent
- **WHEN** a specification declares `Nested nested = DeepMock(verified: false)`
- **THEN** the nested mock that `nested.child` returns is not verified

#### Scenario: Objenesis setting of the parent
- **WHEN** a specification declares `Nested nested = DeepMock(useObjenesis: false)`
- **THEN** the nested mock that `nested.children()` returns does not use Objenesis

#### Scenario: Additional interfaces stay on the parent
- **WHEN** a specification declares `Nested nested = DeepMock(additionalInterfaces: [Runnable])`
- **THEN** `nested` is a `Runnable` and the nested mock that `nested.child` returns is not a `Runnable`

### Requirement: Nested mock names
A nested deep mock SHALL be named after the call that produced it: the name of its parent, a dot, the method name, and the arguments in parentheses. An argument that is a mock SHALL appear as the name of that mock. Other arguments SHALL appear as their Groovy `inspect()` text. When the parent has no name, the simple name of the parent's type SHALL replace the name of the parent.

#### Scenario: Property chain
- **WHEN** a specification declares `Nested nested = DeepMock()` and a feature reads `nested.child.child`
- **THEN** the returned nested mock is named `nested.getChild().getChild()`

#### Scenario: Method with arguments
- **WHEN** a specification declares `Nested nested = DeepMock()` and a feature calls `nested.child('first')`
- **THEN** the returned nested mock is named `nested.child('first')`

#### Scenario: Parent without a name
- **WHEN** a feature creates a deep mock with `DeepMock(Nested)` outside a variable assignment and reads its `child` property
- **THEN** the returned nested mock is named `Nested.getChild()`
