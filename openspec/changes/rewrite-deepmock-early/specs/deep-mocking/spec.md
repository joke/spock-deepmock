## MODIFIED Requirements

### Requirement: Strict mocking with nested chains
An interaction whose target a specification reaches through calls on deep mocks SHALL behave as if the specification declared `_ * parent.call(arguments) >> nested` at the position of the interaction, for every call on the path from the root deep mock to the target. The path SHALL include calls that earlier statements made, for example a nested mock saved in a variable. Spock's normal matching rules SHALL apply to these implicit interactions. Thus code under test can make the calls of a declared chain without matching a catch-all interaction such as `0 * _`.

#### Scenario: Verified chain with catch-all
- **WHEN** a feature declares `1 * nested.child.child.name >> 'Hello'` and `0 * _`, and the code under test calls `nested.getChild().getChild().getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

#### Scenario: Verified chain with arguments
- **WHEN** a feature declares `1 * nested.child('first').child('second').name >> 'Hello'` and `0 * _`, and the code under test calls `nested.child("first").child("second").getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

#### Scenario: Saved nested mock with catch-all
- **WHEN** a `given:` block saves `def second = nested.child`, a `then:` block declares `1 * second.child.name >> 'Hello'` and `0 * _`, and the code under test calls `nested.getChild().getChild().getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

#### Scenario: Catch-all declared before the chain
- **WHEN** a `then:` block declares `0 * _` first and `1 * nested.child.child.name >> 'Hello'` after it, and the code under test calls `nested.getChild().getChild().getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

#### Scenario: Step verification declared after the chain
- **WHEN** a `then:` block declares `1 * nested.child.name >> 'Hello'` first and `1 * nested.child` after it, and the code under test calls `nested.getChild().getName()` once
- **THEN** the feature fails with a `TooFewInvocationsError` for `1 * nested.child`, as it does for a plain Spock `_ * nested.child` interaction declared first

## ADDED Requirements

### Requirement: Same syntax and options as Spock mocks
`DeepMock(...)` SHALL accept every argument form and every option of Spock's `Mock(...)` except `defaultResponse`. `GroovyDeepMock(...)` SHALL accept every argument form and every option of Spock's `GroovyMock(...)` except `defaultResponse`. This SHALL include options maps and types that come from variables.

#### Scenario: Mock maker option
- **WHEN** a specification declares `Nested nested = DeepMock(mockMaker: MockMakers.byteBuddy)`
- **THEN** the mock configuration of `nested` names the `byte-buddy` mock maker

#### Scenario: Interaction closure with a chain
- **WHEN** a specification declares `Nested nested = DeepMock { child.name >> 'Hello' }`
- **THEN** `nested.child.name` returns `'Hello'`

#### Scenario: Type from a variable
- **WHEN** a feature declares `Class type = Nested` and `def nested = DeepMock(type)`
- **THEN** `nested` is a deep mock of type `Nested`

#### Scenario: Options and type from variables
- **WHEN** a feature declares `def options = [name: 'customName']`, `Class type = Nested` and `def nested = DeepMock(options, type)`
- **THEN** `nested` is a deep mock of type `Nested` named `customName`

### Requirement: Chain evaluation counts no invocation
While Spock evaluates the target of an interaction declaration, calls on deep mocks SHALL return their nested mock, SHALL NOT count as invocations for any interaction, and SHALL NOT be recorded as unmatched invocations.

#### Scenario: Step verification declared before the chain
- **WHEN** a `then:` block declares `1 * nested.child` first and `1 * nested.child.name >> 'Hello'` after it, and the code under test calls `nested.getChild().getName()` once
- **THEN** the call returns `'Hello'` and the feature passes

### Requirement: Unmatched calls register no interaction
A call on a deep mock that no interaction matches SHALL return its nested mock or its fallback answer, and SHALL NOT register an interaction.

#### Scenario: Unstubbed chain with ordered then blocks
- **WHEN** a feature declares `1 * other.run()` in a first `then:` block and `1 * another.run()` in a second `then:` block, and the code under test calls the unstubbed `nested.child` twice, then `other.run()`, then `another.run()`
- **THEN** the feature passes without a `WrongInvocationOrderError`

#### Scenario: Unstubbed chain in a cleanup block
- **WHEN** a `cleanup:` block or the `cleanup()` method reads `nested.child.child`
- **THEN** the read returns a nested deep mock and the feature passes

### Requirement: Readable implicit interactions
When a Spock message shows an implicit step interaction, the message SHALL show the call of the step and the line of the interaction declaration that created it.

#### Scenario: Step call after a later ordered block
- **WHEN** a first `then:` block declares `1 * nested.child.name >> 'Hello'`, a second `then:` block declares `1 * other.run()`, and the code under test calls `nested.getChild().getName()`, then `other.run()`, then `nested.getChild()` again
- **THEN** the feature fails with a `WrongInvocationOrderError` whose message contains `nested.getChild()` and the line of `1 * nested.child.name >> 'Hello'`
