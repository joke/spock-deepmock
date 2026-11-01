## 1. Spock 2.5 and examples

- [x] 1.1 Change `spock-bom` in `dependencies/build.gradle` from `2.4-groovy-4.0` to `2.5-groovy-4.0`
- [x] 1.2 Change the compile-only Spock BOM in `spock-deepmock/build.gradle` to `2.5-groovy-4.0`
- [x] 1.3 Move the Groovy BOM to the latest 4.0.x release in `dependencies/build.gradle` and the module build
- [x] 1.4 Remove the `spock2x` list, the `spock24groovy40` source set, its dependencies, the `test$source` task, the `check` hook and the disabled `test` block from `examples/build.gradle`
- [x] 1.5 Run `./gradlew :examples:test` and fix any failure caused by Spock 2.5

## 2. Test JVM arguments

- [x] 2.1 Add `-XX:+EnableDynamicAgentLoading` to the `Test` tasks of `examples` and `spock-deepmock`
- [x] 2.2 Run `./gradlew :examples:test` and confirm the flag reaches the test JVM

## 3. JSpecify generator and null marking

- [x] 3.1 Add `io.github.joke.jspecify:processor:2.0.0` to `annotationProcessor` and `testAnnotationProcessor` in `spock-deepmock/build.gradle`
- [x] 3.2 Add `org.jspecify:jspecify` to `compileOnly` and `testCompileOnly`, at the version the platform pins
- [x] 3.3 Enable Error Prone with `RequireExplicitNullMarking` as an error, and NullAway in `onlyNullMarked` and JSpecify mode
- [x] 3.4 Run `./gradlew :spock-deepmock:compileJava` and fix each nullability finding in main code

## 4. Spotless

- [x] 4.1 Add Spotless with palantir-java-format, forbidden wildcard imports and removed unused imports to `spock-deepmock`
- [x] 4.2 Run `./gradlew :spock-deepmock:spotlessApply` and commit the reformatting as its own commit. The reformatting is committed together with the nullability fixes, by agreement, so it is not a separate commit.

## 5. PMD and CodeNarc

- [x] 5.1 Add the snapshot repository for `io.github.joke.lint`, filtered with `snapshotsOnly()` and `includeGroup`
- [x] 5.2 Add PMD with `rulesets/java/joke-strict.xml` from `io.github.joke.lint:pmd-rules`
- [x] 5.3 Add CodeNarc with `rulesets/groovy/joke-strict.groovy` from `io.github.joke.lint:codenarc-rules`
- [x] 5.4 Run `./gradlew :spock-deepmock:pmdMain :spock-deepmock:codenarcTest` and fix each finding without suppression

## 6. Unit specs and SpockConfig

- [x] 6.1 Copy `SpockConfig.groovy` from `percolate/processor/src/test/resources` to `spock-deepmock/src/test/resources`
- [x] 6.2 Decide the open question on SpockConfig and the no-Mockito scenarios, and record the decision in `design.md`
- [x] 6.3 Add the Spock, Groovy, Mockito and ByteBuddy test dependencies to `spock-deepmock/build.gradle`
- [x] 6.4 Write unit specs for `ChainWalker`, `MockCache`, `DeepMockResponse`, `DeepMockSupport`, `DeepMockInteraction`, `DeepMockTransformation` and `MockingApiExtension`, each tagged `@Tag('unit')`, with Spock mocks for collaborators
- [x] 6.5 Run `./gradlew :spock-deepmock:test` and confirm every specification passes

## 7. Pitest

- [x] 7.1 Add the `info.solidsoft.pitest` plugin, the Pitest junit5 plugin and the `pitest-annotations` compile-only dependency to `spock-deepmock`
- [x] 7.2 Configure Pitest with `includedGroups = ['unit']`, `mutators = ['ALL']`, 100 percent thresholds, and the JVM arguments `-XX:+EnableDynamicAgentLoading` and `-Dspock.parallel.disabled=true`
- [x] 7.3 Run `./gradlew :spock-deepmock:pitest`, record the surviving mutants, and close each gap with a unit spec
- [x] 7.4 For each equivalent mutant that remains, record a reasoned exclusion in `design.md`, and do not add `@DoNotMutate`
- [x] 7.5 Make `check` depend on `pitest` for `spock-deepmock`

## 8. Verify

- [x] 8.1 Run `./gradlew check` and fix every violation. NEVER continue if there are violations!
