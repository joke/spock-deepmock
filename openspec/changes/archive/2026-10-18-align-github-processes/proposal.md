## Why

The release PR for 5.0.0 (#243) was opened under the maintainer's name. The release workflow passes a personal access token (`secrets.RELEASE_PLEASE`) to release-please, so every release PR, and every commit release-please makes, is attributed to a person. The sibling projects percolate and pmd-rules use the default `GITHUB_TOKEN` and their release PRs are authored by `github-actions`. The previous alignment (`align-project-setup`) did not cover this, nor the signoff requirement, snapshot publishing or the branch rules.

## What Changes

- Release-please authenticates with the default `GITHUB_TOKEN`. The `RELEASE_PLEASE` token is no longer referenced.
- **BREAKING** for contributors: the DCO GitHub App is removed. Its replacement is a `Signed-off-by:` check in the `build` job. Pull requests whose commits lack the trailer fail `build`. The check is skipped for release-please branches.
- The release-please `signoff:` input is removed, since release commits no longer need a DCO trailer.
- The build job skips commits whose message starts with `chore: release ` in addition to release-please head branches, matching the siblings.
- The release workflow publishes a `-SNAPSHOT` on every push to main that does not cut a release, using `publishAggregationToCentralSnapshots`, signed with the same key and secrets as the release publish.
- The release workflow accepts a `workflow_dispatch` input that re-publishes an existing release tag.
- `.github/semantic.yml` is kept. Semantic PR stays a required check.
- Branch protection in `.github/settings.yml` is updated: DCO is removed from required checks; `build`, `pre-commit.ci - pr` and `Semantic PR` remain.
- Sibling projects receive the same process changes:
  - percolate: add `.github/semantic.yml`, the Semantic PR required check, the signoff check and the `chore: release ` build condition in `build`, and the `snapshot` job and `workflow_dispatch` input in `release.yml`.
  - pmd-rules: add `.github/semantic.yml`, the Semantic PR required check, and the signoff check in `build`. Its `snapshot` job and `workflow_dispatch` input already exist. Branch protection is still missing and is applied through its settings.
- Existing release PR #243 is closed once the fix is merged, so release-please reopens it under the bot account.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The change affects CI, release and repository settings only. No library behavior or requirement changes.

## Impact

- spock-deepmock: `.github/workflows/build.yml`, `.github/workflows/release.yml`, `.github/settings.yml`, `.github/semantic.yml` (kept), `CONTRIBUTING.adoc` (sign-off wording: the trailer is checked in CI, the DCO app is gone).
- percolate: `.github/workflows/build.yml`, `.github/workflows/release.yml`, `.github/settings.yml`, `.github/semantic.yml` (new).
- pmd-rules: `.github/workflows/build.yml`, `.github/workflows/release.yml`, `.github/settings.yml`, `.github/semantic.yml` (new). Branch protection is not yet applied there; applying `settings.yml` brings it in line.
- Secrets: `RELEASE_PLEASE` can be deleted from spock-deepmock once the change is merged. Snapshot publishing uses the existing Central credentials in each repo.
- Release PR #243: closed after merge. The next release-please run opens a new PR authored by `github-actions`.
- Library code and published coordinates are unchanged.
