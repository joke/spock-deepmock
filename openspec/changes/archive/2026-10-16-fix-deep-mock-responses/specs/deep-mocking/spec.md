## MODIFIED Requirements

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

## ADDED Requirements

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
