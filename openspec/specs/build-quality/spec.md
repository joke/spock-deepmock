# build-quality Specification

## Purpose
TBD - created by archiving change add-module-lint-and-unit-tests. Update Purpose after archive.

## Requirements
### Requirement: Static analysis gates
The `spock-deepmock` module SHALL run PMD with the `joke-strict` ruleset, CodeNarc with the `joke-strict` ruleset for its Groovy specifications, Spotless, Error Prone and NullAway. A violation of any gate SHALL fail `./gradlew check`.

#### Scenario: Java main code violates a PMD rule
- **WHEN** a main Java source file in `spock-deepmock` breaks a rule of the `joke-strict` PMD ruleset
- **THEN** `./gradlew check` fails

#### Scenario: Spock specification violates a CodeNarc rule
- **WHEN** a Spock specification in `spock-deepmock/src/test/groovy` breaks a rule of the `joke-strict` CodeNarc ruleset
- **THEN** `./gradlew check` fails

#### Scenario: Java source is not formatted
- **WHEN** a Java source file in `spock-deepmock` does not match the palantir-java-format style
- **THEN** `./gradlew check` fails

### Requirement: Null marking
Every package of the `spock-deepmock` module SHALL be `@NullMarked`. The JSpecify generator SHALL write the package metadata, and Error Prone with `RequireExplicitNullMarking` SHALL fail the build for any package that is not marked.

#### Scenario: Package without null marking
- **WHEN** a package of `spock-deepmock` has no `package-info.java` and the JSpecify generator does not write one
- **THEN** Error Prone fails the build

#### Scenario: Null-marked package
- **WHEN** the JSpecify generator runs on `spock-deepmock`
- **THEN** `package-info.class` for `io.github.joke.spockdeepmock` carries `@NullMarked`

### Requirement: Mutation coverage
The `spock-deepmock` module SHALL run Pitest with a line coverage, mutation score and test strength of 100 percent for the `unit` tag. A value below 100 percent SHALL fail `./gradlew check`.

#### Scenario: Surviving mutant
- **WHEN** a Pitest mutant of a main class survives the `unit` tests
- **THEN** `./gradlew check` fails

#### Scenario: Only unit-tagged specifications run
- **WHEN** Pitest runs
- **THEN** it runs only specifications tagged `unit`

### Requirement: Unit test suite
The `spock-deepmock` module SHALL provide Spock specifications under `src/test/groovy`. Each specification SHALL carry the `unit` tag and SHALL use Spock mocks for its collaborators.

#### Scenario: Specification without the unit tag
- **WHEN** a specification in `spock-deepmock/src/test/groovy` has no `@Tag('unit')`
- **THEN** Pitest does not run it, and the module's coverage check fails

#### Scenario: Collaborator mocked with Spock
- **WHEN** a unit specification tests a class that depends on another type
- **THEN** the dependency is a Spock `Mock()`

### Requirement: Mockito on test JVMs
Every test JVM in the build that runs Mockito, and the Pitest JVM, SHALL start with `-XX:+EnableDynamicAgentLoading`.

#### Scenario: Examples run Mockito
- **WHEN** `examples` runs its specifications
- **THEN** the test JVM starts with `-XX:+EnableDynamicAgentLoading`

#### Scenario: Pitest runs
- **WHEN** Pitest runs for `spock-deepmock`
- **THEN** the Pitest JVM starts with `-XX:+EnableDynamicAgentLoading`

### Requirement: Spock configuration
The `spock-deepmock` test sources SHALL include the `SpockConfig.groovy` file from percolate. Spock SHALL disable parallel specifications when the system property `spock.parallel.disabled` is `true`, and SHALL apply a global timeout of one minute to each feature.

#### Scenario: Pitest sets the parallel property
- **WHEN** Pitest runs for `spock-deepmock`
- **THEN** the JVM property `spock.parallel.disabled` is `true`

### Requirement: Examples run as a normal Spock module
The `examples` module SHALL run its Spock specifications through the standard `test` task. It SHALL NOT define a source set or task per Spock version.

#### Scenario: Examples test task
- **WHEN** `./gradlew :examples:test` runs
- **THEN** every specification under `examples/src/test/groovy` runs

#### Scenario: No version matrix
- **WHEN** the build is configured
- **THEN** `examples` defines no source set named after a Spock version

### Requirement: Spock version
The build SHALL use Spock `2.5-groovy-4.0` for every module that has Spock specifications, and Groovy 4.0.x.

#### Scenario: Dependency resolution
- **WHEN** the build resolves the test classpath of `spock-deepmock` or `examples`
- **THEN** `spock-core` resolves to `2.5-groovy-4.0`
