# Proposal

## Why

The library is compiled by the Groovy joint compiler although five of its six source files are already Java, and the only Groovy file (`MockCache.groovy`) uses `@Memoized`, which keeps one JVM-wide cache that retains every deep mock and every specification instance of a test run. The sibling libraries use Java with Lombok as their primary language, and a plain javac build is the precondition for adding the PMD rules later. The next release is a major release anyway (Spock 2.4 is already required on `main`), so this is the moment to also fix the deep-mock behavior that a Java rewrite of the cache exposes.

## What Changes

- Move the library sources from `src/main/groovy/` to `src/main/java/` and port `MockCache.groovy` to Java. The library module no longer applies the `groovy` plugin; Groovy stays a `compileOnly` dependency through `spock-core`.
- The library is compiled by javac with the convention plugin's `release = 8`, `-parameters` and `-Werror` for the first time (these options apply to `JavaCompile` only and were never applied by `compileGroovy`).
- Use Lombok for boilerplate, wired as in the sibling libraries (`io.freefair.lombok`, Lombok 1.18.46, a root `lombok.config`).
- Call the `SpecInternals.MockImpl` and `GroovyMockImpl` overloads directly instead of through `GroovyRuntimeUtil.invokeMethod` reflection, and stop mutating the options map the caller passes in.
- **BREAKING**: `GroovyDeepMock()` creates a Groovy mock. Before, every `GroovyDeepMockImpl` overload delegated to `MockImpl`, so the top-level mock was a Java mock.
- **BREAKING**: A nested deep mock is identified by the call that produced it (target mock, method, parameter types and arguments). Before, the cache key had no method and no arguments, and an existing interaction matched a call by method name and arguments only. Overloads with equal arguments, such as `lookup(String)` and `lookup(Object)`, could therefore return the same nested mock.
- **BREAKING**: Deep mocks are cached per specification instance in Spock's extension store and are released when Spock closes that store. Before, one static cache held them for the whole JVM.
- **BREAKING**: A deep mock call whose return type is a primitive, an array or a final class (for example `String`) throws `InvalidSpecException` with a message that names the call and suggests stubbing it. Before, it failed with Spock's `CannotCreateMockException`.
- Remove the unused `auto-service` annotation processor and the commented-out `sourceSets` block from the library build.
- Release as 5.0.0: the commits of this change mark the breaking changes with `!` or a `BREAKING CHANGE:` footer, because release-please would otherwise not cut a major version.

Out of scope: JSpecify and the `io.github.joke.jspecify:processor` (they conflict with `release = 8` and `-Werror`; see design.md), raising the bytecode baseline, PMD, CodeNarc and NullAway.

## Capabilities

### New Capabilities

- `deep-mocking`: creating deep mocks with `DeepMock()` and `GroovyDeepMock()`, the mock implementation they use, the identity of nested mocks per call, and the lifetime of nested mocks within a specification instance.

### Modified Capabilities

None. `openspec/specs/` has no capabilities yet.

## Impact

- Library sources: `spock-deepmock/src/main/groovy/` moves to `spock-deepmock/src/main/java/`; `MockCache.groovy` becomes `MockCache.java`; `DeepMockResponse`, `DeepMockInteraction`, `SpecInternalsExtension` and `MockingApiExtension` change.
- Build: `spock-deepmock/build.gradle`, `settings.gradle` (Lombok plugin version), `buildSrc` convention plugin (Lombok version), `dependencies/build.gradle` (Lombok 1.18.46, auto-service removed), new root `lombok.config`.
- Tests: new example specifications under `examples/src/test/groovy/`.
- Consumers: `GroovyDeepMock()` returns Groovy mocks; nested mocks for different calls are different objects; nested mocks are not shared between specification instances. The public `DeepMock()` and `GroovyDeepMock()` signatures are unchanged and the bytecode stays Java 8.
- Release: the next release is 5.0.0.
