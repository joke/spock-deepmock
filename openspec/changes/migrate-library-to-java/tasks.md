# Tasks

## 1. Example fixtures and failing specifications

- [x] 1.1 Add a second method with the return type `Nested` to `Nested` and `GroovyNested`, and add the overloads `lookup(String)` and `lookup(Object)` with different return types; verify `./gradlew :examples:compileSpock24groovy40Groovy` succeeds.
- [x] 1.2 Add a specification for the mock implementation (top-level `DeepMock()` is `JAVA`, top-level `GroovyDeepMock()` is `GROOVY`, nested mocks by return type) with `MockUtil.asMock(...).getMockImplementation()`; verify that the `GroovyDeepMock()` feature fails on the current code.
- [x] 1.3 Add a specification for the nested mock identity per call (repeated access, saved mock, different methods, different arguments, equal arguments, overloads with equal arguments, and the same calls across interaction scopes). The overloads features fail on the current code, because an existing interaction matches by method name and arguments only. The other features are regression guards: they pass on the current code.
- [x] 1.4 Add a specification for the options handling (`defaultResponse` is rejected, the caller's options map stays unchanged); verify that the "options unchanged" feature fails on the current code.
- [x] 1.5 Add a specification for the lifetime and threads (separate features get separate nested mocks, several threads get the same nested mock, a nested deep mock call in a `cleanup:` block and in `cleanup()` works, `toString()` returns no mock); record which features fail on the current code.

## 2. Java sources and build

- [x] 2.1 Move all files from `spock-deepmock/src/main/groovy/` to `spock-deepmock/src/main/java/` with `git mv`; verify that `spock-deepmock/src/main/groovy/` does not exist.
- [x] 2.2 Port `MockCache.groovy` to `MockCache.java` with the same behavior and no `@Memoized`; verify that `grep -r Memoized spock-deepmock/src` finds nothing.
- [x] 2.3 Remove the `groovy` plugin, the `auto-service` dependencies and the commented-out `sourceSets` block from `spock-deepmock/build.gradle`; verify `./gradlew :spock-deepmock:compileJava` runs javac with `-Werror` and succeeds.
- [x] 2.4 Fix each javac warning in the code (risk R1); use `@SuppressWarnings` only on the one statement that needs it; verify the compile has no warnings.
- [x] 2.5 Verify that the compiled classes are Java 8 bytecode (major version 52) with `javap -v`.

## 3. Lombok

- [x] 3.1 Declare `io.freefair.lombok` 9.5.0 in `settings.gradle`, set `lombok.version = '1.18.46'` in the convention plugin, and set Lombok 1.18.46 and remove `auto-service` in `dependencies/build.gradle`; verify `./gradlew help` configures.
- [x] 3.2 Add the root `lombok.config` with `config.stopBubbling`, `lombok.addLombokGeneratedAnnotation` and `lombok.experimental.flagUsage = ALLOW`, without `lombok.addNullAnnotations`; verify that the file has three entries.
- [x] 3.3 Apply `io.freefair.lombok` in the library and add `annotationProcessor` and `compileOnly` Lombok through the `dependencies` platform; verify `./gradlew :spock-deepmock:compileJava` succeeds.
- [x] 3.4 Apply the Lombok annotations from design D3; verify that no hand-written constructor or private utility constructor remains that Lombok can generate.
- [x] 3.5 Run the existing `MockTest` and `GroovyMockTest` (risk R2); verify that they pass and that the specifications from group 1 fail only where 1.2 to 1.5 expect it.

## 4. Behavior changes

- [x] 4.1 Change `SpecInternalsExtension` to call `SpecInternals.MockImpl` and `SpecInternals.GroovyMockImpl` directly, with `GroovyMockImpl` in every `GroovyDeepMockImpl` overload (design D6); verify that the `GroovyDeepMock()` feature from 1.2 passes.
- [x] 4.2 Copy the options into a new map before `defaultResponse` is added, and remove the use of `org.apache.groovy.util.Maps` (design D7); verify that the specification from 1.4 passes.
- [x] 4.3 Add the parameter-types constraint to `DeepMockInteraction` and build its constraints in a static factory (design D5); verify that the library compiles.
- [x] 4.4 Change `MockCache` to a list of `DeepMockInteraction` entries with a `synchronized` find-or-create method (design D5); verify that the specification from 1.3 passes.
- [x] 4.5 Make `DeepMockResponse` stateless and get the `MockCache` from the store of the mock's specification instance (design D4); remove the static cache; verify that the specification from 1.5 passes.
- [x] 4.6 If a call in a `cleanup:` block or in `cleanup()` fails because the store is closed (risk R3), stop and report the failure before you change the design.

## 5. Packaging

- [x] 5.1 Run `./gradlew :spock-deepmock:javadocJar` and examine the javadoc output for Lombok-generated members (risk R5); verify the task succeeds.
- [x] 5.2 Run `./gradlew publishToMavenLocal` and compare the jar with the previous jar; verify that the top-level class names and the `META-INF/services` files are the same and that the POM has no Lombok dependency. The jar is not expected to be identical: a new constraint class is added and the Groovy closure class of `@Memoized` is removed.

## 6. Release

- [x] 6.1 Write the commit for the behavior changes with `feat!:` and a `BREAKING CHANGE:` footer that names the `GroovyDeepMock()` change, the nested mock identity per call, the cache per specification instance and the move to Spock 2.4 (design D10); verify with `git log -1` that the footer exists.

## 7. Verification

- [x] 7.1 Run `./gradlew check`; verify that it passes with no violations, no warnings and no test failures. Do not continue if a violation exists.
