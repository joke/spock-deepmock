# Tasks

## 1. Toolchain and wrapper

- [x] 1.1 Create `.mise.toml` pinning `java = "liberica-17.0.20.1+1"` (JDK 25 was replaced per R2) and `"npm:@fission-ai/openspec" = "1.3.1"`; verify `mise install` provisions both tools.
- [x] 1.2 Upgrade the Gradle wrapper to 9.6.1 in `gradle/wrapper/gradle-wrapper.properties`; verify `./gradlew --version` reports 9.6.1.

## 2. Convention plugin in buildSrc

- [x] 2.1 Create `buildSrc/build.gradle` with `groovy-gradle-plugin` and the dependencies `io.github.joke.conventional-version:io.github.joke.conventional-version.gradle.plugin:2.1.0` and `io.github.sgtsilvio.gradle:gradle-metadata:0.7.0`; verify `./gradlew help` configures without error.
- [x] 2.2 Create `buildSrc/src/main/groovy/spock-deepmock.conventions.gradle` with group, Maven Central repository, `release = 8`, `-parameters`, `-Werror` and UTF-8 for `java-base`; verify `./gradlew compileJava` succeeds on Java 8 bytecode (check with `javap -v` that the major version is 52).
- [x] 2.3 Add `maven-publish`, `signing` with `useGpgCmd()`, javadoc and sources jars, and the metadata plugin to the convention plugin; verify `./gradlew publishToMavenLocal` produces the jar, sources, javadoc and POM.
- [x] 2.4 Apply the convention plugin from the root `build.gradle` to the subprojects and remove the `allprojects` block; verify `./gradlew projects` lists the same subprojects as before.
- [x] 2.5 Handle risk R1: if `./gradlew compileJava` fails with an obsolete-source warning under `-Werror`, add `-Xlint:-options` to the convention plugin; verify the compile passes.

## 3. Versioning and release-please

- [x] 3.1 Remove `org.shipkit.shipkit-auto-version` from the root build; verify no reference to shipkit remains with `grep -r shipkit`.
- [x] 3.2 Add `release-please-config.json` (`release-type: simple`, package `.`) and `.release-please-manifest.json` with `"." : "4.0.0"`; verify both files are valid JSON.
- [x] 3.3 Verify that the version derived from tags is `4.0.0` on the released commit and does not pick up the unprefixed tags (risk R3); verify with `./gradlew :spock-deepmock:properties --property version`.

## 4. Central Portal publishing

- [x] 4.1 Add `com.gradleup.nmcp.settings` (1.6.1) to `settings.gradle` with `nmcpSettings` centralPortal credentials from `mavenCentralUsername` and `mavenCentralPassword` gradle properties; verify `./gradlew help` configures.
- [x] 4.2 Configure `nmcpAggregation` in the root build and remove the OSSRH repositories block; verify `./gradlew publishAggregationToCentralPortal --dry-run` lists the publishable subprojects.
- [x] 4.3 Compare the generated POM with the published 4.0.0 POM; verify coordinates, license, developers and SCM match. Accepted deviation: the SCM block uses the correct `https://github.com/joke/spock-deepmock.git` instead of the published `git://` and `tree/master` values; the developer email stays `joke@xckk.de`.

## 5. Gradle properties and hygiene

- [x] 5.1 Update `gradle.properties` with the caching, configuration-cache, configure-on-demand, parallel and problems-report flags; remove the committed GPG key ID and empty credential keys; verify `./gradlew check` still runs.
- [x] 5.2 Update `.pre-commit-config.yaml` to `pre-commit-hooks` v6.0.0 and `language-formatters` v2.16.0, add the extra file-hygiene hooks, and keep `conventional-pre-commit`; verify `pre-commit run --all-files` passes.
- [x] 5.3 Update `.gitignore` with `.claude/*`, `!.claude/skills/`, `bin/`, `.project`, `.classpath`, `.settings`, `.vscode/` and `.worktrees/`; verify `git status` no longer lists machine-local `.claude` files.
- [x] 5.4 Change the README badge branch from `master` to `main`; verify with `grep -n master README.adoc`.

## 6. CI workflows

- [x] 6.1 Rewrite `.github/workflows/build.yml` with `actions/checkout@v7` (`fetch-depth: 0`), `jdx/mise-action@v4`, `gradle/actions/setup-gradle@v6`, `./gradlew check`, and the test reporter step; verify the workflow file parses as YAML.
- [x] 6.2 Rewrite `.github/workflows/release.yml` with the release-please job and a `publish` job gated on `release_created` that imports the GPG key and runs `./gradlew publishAggregationToCentralPortal`; verify the file parses as YAML and the job gates match.
- [ ] 6.3 Note the manual step for the repository secrets (`mavenCentralUsername`, `mavenCentralPassword`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`, `GPG_FINGERPRINT`) in the pull request description, since they cannot be created from the repository (risk R5).

## 7. OpenSpec rules

- [x] 7.1 Add the `rules` block to `openspec/config.yaml` with the design rules (Simplified Technical English, mermaid diagrams) and the tasks rule requiring a final `./gradlew check` task; verify `openspec list --json` still reports the project root.

## 8. Integration check

- [x] 8.1 Run `./gradlew clean check` under the pinned toolchain and verify it passes; if it fails under JDK 25 with Groovy 4.0.6 (risk R2), pin 17 in `.mise.toml` and rerun.
- [x] 8.2 Run `./gradlew publishToMavenLocal` and verify the published artifact set and POM are unchanged except for the version source.

## 9. Spock 2.4 on Groovy 4.0

- [x] 9.1 Bump `spock-bom` to `2.4-groovy-4.0` in `dependencies/build.gradle` and `spock-deepmock/build.gradle`; verify the library compiles against Spock 2.4.
- [x] 9.2 Adapt to the move of `SpecInternals` to `org.spockframework.runtime`; verify the import resolves and the library compiles.
- [x] 9.3 Make the DSL extension take a `Class` receiver and a `Specification` argument, and call `SpecInternals.MockImpl` and `GroovyMockImpl` directly from `MockCache`, since `Specification.createMock` no longer exists; verify `DeepMock()` in a field initializer and `DeepMock(name: ...)` create mocks in the examples.
- [x] 9.4 Adapt `DeepMockInteraction.accept` to return a `Supplier<Object>`; verify the library compiles.
- [x] 9.5 Reduce the examples matrix to `spock24groovy40` and keep the JUnit launcher on its runtime classpath; verify `./gradlew clean check` passes with 14 tests and no failures.
- [x] 9.6 Change the README support line to Spock 2.4 on Groovy 4.0; verify with `grep -n "Works with" README.adoc`.
- [x] 9.7 Verify that the library and the Spock 2.4 Groovy 4 classes are Java 8 bytecode (major version 52); verify with `od` on the compiled classes.

## 10. Gradle deprecations

- [x] 10.1 Replace `sourceSets.create`, `tasks.create`, `sourceSets.findByName` and the unscoped `tasks.withType` in `examples/build.gradle` with `register`, `named` and `configureEach`, and attach the matrix test tasks to `check` outside their configure blocks; verify `./gradlew clean check --warning-mode all` reports no deprecations and runs the 14 tests.
- [x] 10.2 Upgrade `com.gradleup.nmcp.settings` from 1.6.1 to 1.6.2, which removes the "Project object as a dependency notation" deprecation raised by the plugin; verify `./gradlew :dependencies:help --warning-mode all` reports none.
