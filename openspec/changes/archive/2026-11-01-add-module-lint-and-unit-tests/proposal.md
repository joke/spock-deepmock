## Why

The `spock-deepmock` module has no unit tests and no static analysis. Its only tests live in `examples/`, which pitest cannot see, so nothing enforces coverage or lint rules on the library code. The sibling projects percolate and pmd-rules already run these gates. This change brings the same gates to this module.

## What Changes

- Add PMD with the `joke-strict.xml` ruleset from `io.github.joke.lint:pmd-rules`.
- Add CodeNarc with the `joke-strict.groovy` ruleset from `io.github.joke.lint:codenarc-rules` for the Spock specifications.
- Add Pitest with a 100/100/100 floor and `includedGroups = ['unit']`, wired into `check`.
- Add Spotless with palantir-java-format, forbidden wildcard imports and removed unused imports.
- Add Error Prone with `RequireExplicitNullMarking` as an error, and NullAway in `onlyNullMarked` and JSpecify mode.
- Add the JSpecify generator `io.github.joke.jspecify:processor:2.0.0` as an annotation processor for the module, with `org.jspecify:jspecify` on the compile classpath.
- Copy `SpockConfig.groovy` from percolate into `spock-deepmock/src/test/resources`.
- Add unit specs under `spock-deepmock/src/test/groovy`, tagged `@Tag('unit')`, using Spock mocks for collaborators.
- Move the Spock BOM from `2.4-groovy-4.0` to `2.5-groovy-4.0`, and move the Groovy BOM to the 4.0.x line.
- Simplify `examples/`: remove the `spock24groovy40` source set, its test tasks and the disabled default `test` task, so examples run as a normal Spock module.
- Pass `-XX:+EnableDynamicAgentLoading` to test JVMs that use Mockito, and to Pitest.
- **BREAKING**: none for library consumers. The change affects build and test configuration only.

## Capabilities

### New Capabilities
- `build-quality`: Static analysis, mutation coverage and unit-test requirements for the `spock-deepmock` module.

### Modified Capabilities
- None. No `deep-mocking` requirement changes.

## Impact

- **Build files:** `dependencies/build.gradle`, `spock-deepmock/build.gradle`, `examples/build.gradle`, and a new `spock-deepmock.conventions` entry for the snapshot repository used by the lint artifacts.
- **Source tree:** new `spock-deepmock/src/test/groovy` and `spock-deepmock/src/test/resources`. The module's main code is reformatted by Spotless and must pass NullAway and Error Prone.
- **Dependencies:** `io.github.joke.lint:pmd-rules` and `codenarc-rules` from the snapshot repository; `io.github.joke.jspecify:processor:2.0.0`; Spock 2.5 and Groovy 4.0.x; Pitest, Mockito and ByteBuddy on the test classpath.
- **CI:** `./gradlew check` now runs Pitest, which is slower.
- **Open decision:** whether the examples use Mockito as the mock maker, as direct Mockito collaborators, or both. This is recorded in the design.
