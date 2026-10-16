# Design

## Context

- The root build applies `allprojects` with `pluginManager.withPlugin` blocks and sets Java 8 source and target. It is a plain Gradle 7.5.1 build with the `shipkit-auto-version` plugin deciding the version.
- Publishing goes to the legacy OSSRH staging URL with GPG through `useGpgCmd()`. The POM is written by hand in `build.gradle`.
- CI runs on Zulu 17 with `setup-java`. Nothing pins the local toolchain.
- The release workflow runs release-please with no config file and then `./gradlew publish`. Tags are `v3.0.0` and `v4.0.0` (`2.0.0`, `2.0.1` and `1.1.0` are older, unprefixed tags).
- The sibling projects use `buildSrc/` convention plugins. Their version comes from the `io.github.joke.conventional-version` plugin, their publishing from the `com.gradleup.nmcp.settings` Central Portal plugin, and their release from a release-please manifest.
- See proposal.md for the motivation.

## Goals / Non-Goals

**Goals:**
- Build, toolchain, CI and release match the sibling projects' baseline.
- The published artifacts keep their coordinates and their Java 8 bytecode.
- Version is derived from git tags, and release-please owns the release cut.

**Non-Goals:**
- No Java 11 or Groovy 5 upgrade. Bytecode stays at `release = 8`. Spock moves to 2.4 on Groovy 4.0 (see D13).
- No Antora, Pitest, CPD, architecture rules, PMD, CodeNarc, Spotless, Error Prone or NullAway wiring.
- No library code changes.

## Decisions

**D1. Bytecode stays at release 8.** The copied convention plugin sets `options.release = 8`, not the 11 used by the siblings. Alternative considered: move to 11 as the siblings do. Rejected because it drops Java 8 consumers, which is outside this change.

**D2. Gradle wrapper 9.6.1.** Matches the siblings. Gradle 9 runs on JDK 17+, so the CI and local JDKs must be 17 or later, which the chosen toolchain satisfies.

**D3. Toolchain pinned in `.mise.toml`.** `java = "liberica-17.0.20.1+1"` and `"npm:@fission-ai/openspec" = "1.3.1"`. The siblings pin 25, but Groovy 4.0.6 cannot compile against JDK 25 class files (major version 69), so this project pins 17 (risk R2, fallback applied during implementation). Gradle 9.6.1 runs on 17, and the bytecode target stays 8.

**D4. Version from tags, cut by release-please.**
- Replace `org.shipkit.shipkit-auto-version` with `io.github.joke.conventional-version` (2.1.0) in the convention plugin.
- Add `release-please-config.json` (`release-type: simple`, package `.`) and `.release-please-manifest.json` with `"." : "4.0.0"`, the last released tag.
- Alternative: keep shipkit and release-please side by side. Rejected because two tools would own the version.

**D5. Publishing through Central Portal.**
- Add `com.gradleup.nmcp.settings` (1.6.2) to `settings.gradle` with `publishingType = 'AUTOMATIC'` and credentials from `mavenCentralUsername` and `mavenCentralPassword` gradle properties.
- Configure `nmcpAggregation` in the root build and publish with `publishAggregationToCentralPortal`.
- Remove the OSSRH repositories block and the hand-written POM. The `io.github.sgtsilvio.gradle.metadata` plugin (0.7.0) supplies license, developer and SCM metadata, as in the siblings.
- Keep `useGpgCmd()`. The release job imports the key, as the siblings do.

**D6. `buildSrc` convention plugin.** Create `buildSrc/` with `groovy-gradle-plugin` and `src/main/groovy/spock-deepmock.conventions.gradle`. It contains:
- group, `repositories`, `java-base` compile options (`release = 8`, `-parameters`, `-Werror`, UTF-8);
- `maven-publish`, `signing` with `useGpgCmd()`, and javadoc and sources jars;
- the Central Portal and metadata wiring from D5 and D4.

It does not contain Spotless, Error Prone, NullAway, PMD, CodeNarc, Pitest, CPD, Antora or architecture rules. "Linting rules" is read here as all static-analysis wiring. If you meant only PMD and CodeNarc, Spotless, Error Prone and NullAway would be added back later as a separate change.

**D7. Root build reduced to plugin application.** The root `build.gradle` applies only `idea` and the nmcp aggregation config. Each module applies `spock-deepmock.conventions` by id in its `plugins` block. The `allprojects` block moves into `buildSrc`.

**D8. CI on current actions.** `build.yml` uses `actions/checkout@v7` with `fetch-depth: 0`, `jdx/mise-action@v4`, `gradle/actions/setup-gradle@v6`, and `./gradlew check`, then publishes test results with `dorny/test-reporter@v3`. The badge branch in `README.adoc` changes from `master` to `main`.

**D9. Release workflow.** `release.yml` runs release-please (`GoogleCloudPlatform/release-please-action@v5.0`). The `publish` job runs only when `release_created` is true. It checks out the release tag, imports the GPG key, and runs `./gradlew publishAggregationToCentralPortal`. There is no docs or Pages job.

**D10. Gradle speed flags and properties.** `gradle.properties` gets `org.gradle.caching=true`, `org.gradle.configuration-cache=true`, `org.gradle.configureondemand=true`, `org.gradle.parallel=true`, and `org.gradle.problems.report=true`. The committed GPG key ID and empty credential keys are removed.

**D11. Hygiene.** Update pre-commit hook revisions to the siblings' (`pre-commit-hooks` v6.0.0, `language-formatters` v2.16.0) and add their extra file-hygiene hooks. Keep `conventional-pre-commit`, which the siblings do not have and which this project uses. `.gitignore` gets the siblings' `.claude/*` and `!.claude/skills/` rule, plus `bin/`, `.project`, `.classpath`, `.settings`, `.vscode/` and `.worktrees/`.

**D12. OpenSpec rules.** `openspec/config.yaml` gets a `rules` block copied from the sibling pattern: design rules use Simplified Technical English and mermaid diagrams, and the tasks rule requires a final `./gradlew check` task.

**D13. Spock 2.4 on Groovy 4.0.** `spock-bom` and the `dependencies` platform move to `2.4-groovy-4.0`. The 2.4 line on Groovy 5 is rejected because Groovy 5 classes are Java 11 bytecode, which would break Java 8 consumers' test runs. Verified: the Spock 2.4 Groovy 4 classes and the library are both Java 8 bytecode. Two Spock API changes are adapted in the library:
- `SpecInternals` moved from `org.spockframework.lang` to `org.spockframework.runtime`. The import is updated.
- The mock helpers `MockImpl` and `GroovyMockImpl` are static on `SpecInternals` and take the `Specification` as their first argument. The DSL extension methods take a `Class` receiver, so Groovy dispatches the rewritten static calls to them, and `MockCache` calls the helpers directly. `Specification.createMock` no longer exists.
- `IMockInteraction.accept` returns a `Supplier<Object>`. `DeepMockInteraction` returns one.

The examples matrix shrinks from 10 Spock 2.x/Groovy combinations to one (`spock24groovy40`). Cost: older Spock versions are no longer tested.

## Risks / Trade-offs

- **R1. JDK 25 with `--release 8` and `-Werror`.** Recent javac versions may print an "obsolete source value 8" warning, which `-Werror` turns into a build failure. Mitigation: verify the first build. If it fails, add `-Xlint:-options` to the convention plugin rather than changing the toolchain.
- **R2. Groovy 4.0.6 on JDK 25.** Groovy 4 may not run cleanly on JDK 25. Mitigation: run `./gradlew check` under the pinned toolchain. If it fails, pin 17 in `.mise.toml`, which Gradle 9 also supports. Outcome: it occurred (Groovy 4.0.6 cannot read JDK 25 class files, major version 69), and the JDK 17 pin was applied.
- **R3. Version change.** `conventional-version` derives the version from tags. Its tag prefix must match `v4.0.0`, and the older unprefixed tags must not be picked up. Mitigation: verify with `./gradlew properties` before any release-please run.
- **R4. First release-please run.** If the manifest does not match the last tag, release-please could cut a duplicate version. Mitigation: manifest is `4.0.0`, and the first run is checked before merge.
- **R5. Secret names.** The Central Portal credentials and the GPG secrets have new names (`mavenCentralUsername`, `mavenCentralPassword`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`, `GPG_FINGERPRINT`). Those secrets must exist in the GitHub repository. Adding them is a manual step outside this change.
- **R6. Tests and javadoc under the new build.** Javadoc and sources jars are now built by the convention plugin, so javadoc errors can fail `check`. Mitigation: verify in the first build.

- **R7. Examples matrix under Gradle 9.** The matrix sets enforced the root `dependencies` platform, which conflicts with each set's own `spock-bom` under Gradle 9. Fix applied: each matrix set takes the platform as a soft `platform(...)` (its enforced Spock and Groovy BOMs still win), and adds `junit-platform-launcher` as a runtime dependency, which Gradle 9 no longer provides implicitly. Verified: `clean check` passed with the configuration cache on, 140 tests, the same count as `HEAD`. Later, the matrix was reduced to `spock24groovy40` (see D13), so the count is now 14.

**D14. Gradle deprecations.** The examples build now uses lazy `sourceSets.register`, `tasks.register`, `sourceSets.named` and `tasks.withType(...).configureEach` in place of the eager `create`, `findByName` and unscoped `withType`. The matrix test tasks are attached to `check` outside their configure blocks: attaching inside a lazy `register` block leaves `check` without the tests. The nmcp plugin is upgraded to 1.6.2, which removes its "Project object as a dependency notation" deprecation. Verified: `clean check --warning-mode all` reports no deprecations.

## Migration Plan

1. Apply the build, CI and config changes on a branch.
2. Verify locally with `./gradlew check` under the pinned toolchain, then with `./gradlew publishToMavenLocal` to confirm the POM and coordinates are unchanged.
3. Compare the generated POM with the currently published 4.0.0 POM. Coordinates, license, developers and SCM must match.
4. Add the Central Portal and GPG repository secrets, and confirm they exist.
5. Merge. The first release-please run is checked before its release PR is merged.

**Rollback:** revert the change commit. The manifest and the workflows return to their previous state. No published artifact is touched until a release tag is cut.

## Open Questions

None. The remaining unknowns are verified during implementation (R1, R2, R3).
