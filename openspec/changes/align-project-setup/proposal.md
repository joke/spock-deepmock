# Proposal

## Why

The build, toolchain, CI and release setup of spock-deepmock date from 2022-2023. The sibling projects percolate and pmd-rules have moved to a shared baseline: a pinned toolchain, current Gradle, current CI actions, release-please versioning and Central Portal publishing. Keeping spock-deepmock on the old setup makes releases manual and error-prone, and it drifts further from the conventions used across these projects.

## What Changes

- Upgrade the Gradle wrapper from 7.5.1 to 9.6.1.
- Pin the toolchain with a `.mise.toml` (Java and OpenSpec), matching the sibling projects.
- Keep the bytecode target at `release = 8`. No Java bump.
- Move Spock from 2.3 to 2.4 on the Groovy 4.0 line (`spock-bom:2.4-groovy-4.0`), so consumers on Java 8 keep working. The two Spock API changes the library depends on are adapted; the examples matrix is reduced to Spock 2.4 only.
- Replace the shipkit-auto-version plugin with release-please: add `release-please-config.json` and `.release-please-manifest.json`, and align the release workflow.
- Publish through the Central Portal plugin (`com.gradleup.nmcp.settings`) instead of the legacy OSSRH staging endpoint and in-build GPG configuration.
- Update CI workflows to current actions (checkout, mise, setup-gradle, test reporter) and run `./gradlew check`.
- Add the Gradle speed settings used by the siblings (configuration cache, build cache, parallel).
- Copy the `buildSrc` convention-plugin approach from the siblings and move the build logic into it.
- Update pre-commit hook revisions and hygiene hooks; keep the conventional-commit check.
- Align `.gitignore` with the siblings: ignore machine-local `.claude/*` while keeping `.claude/skills/`, and ignore IDE and worktree directories.
- Add the OpenSpec `rules` block to `openspec/config.yaml` (design and task rules), in line with the siblings.
- Remove the committed GPG key ID and empty credential keys from `gradle.properties`; credentials come from properties supplied outside the repo.
- Correct the README badge branch from `master` to `main`.

Out of scope: Antora documentation site, Pitest, CPD, architecture and lint rules, and a Java 11 or Groovy 5 bump.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This change alters build, release and tooling configuration only. No library behavior or requirement changes, so the change opts out of specs with `skip_specs: true`.

## Impact

- Build: `build.gradle`, `settings.gradle`, `gradle.properties`, `buildSrc/` (new), `gradle/wrapper/`.
- Toolchain: `.mise.toml` (new).
- CI and release: `.github/workflows/build.yml`, `.github/workflows/release.yml`, `release-please-config.json` and `.release-please-manifest.json` (new).
- Hygiene: `.pre-commit-config.yaml`, `.gitignore`, `README.adoc`.
- OpenSpec: `openspec/config.yaml`.
- Published artifacts: the publication coordinates stay the same. The release version is now owned by release-please, so the next release version must be checked against the existing `v*` tags before the first release-please run.
- Consumers: the library now compiles against Spock 2.4 internals, so consumers must run Spock 2.4 on Groovy 4.0 instead of 2.3. Bytecode stays Java 8, and the public `DeepMock()` and `GroovyDeepMock()` API is unchanged.
- Library sources: `spock-deepmock/src/main/groovy/` (three files adapted to Spock 2.4).
- Examples: `examples/build.gradle` (matrix reduced to Spock 2.4).
