## 1. Release token

- [ ] 1.1 Remove `token: ${{ secrets.RELEASE_PLEASE }}` and `signoff:` from the release-please step in `.github/workflows/release.yml`; verify the file parses as YAML.

## 2. Snapshots and re-publish

- [ ] 2.1 Verify that the nmcp plugin in `settings.gradle` provides `publishAggregationToCentralSnapshots`; run `./gradlew tasks --all | grep -i snapshot` and record the task name.
- [ ] 2.2 Add a `snapshot` job to `release.yml`. It needs `release-please`, runs on `push` when `releases_created` is not `'true'`, imports the GPG key with the same secrets as `publish`, and runs the snapshot task from 2.1.
- [ ] 2.3 Add `workflow_dispatch` with a required `tag` input to `release.yml`. Let the `publish` job check out `inputs.tag || github.sha` and accept `workflow_dispatch` in its `if`.

## 3. Build workflow

- [ ] 3.1 Add the `!startsWith(github.event.head_commit.message, 'chore: release ')` condition to the `build` job in `build.yml`, combined with the existing release-please head-branch condition.
- [ ] 3.2 Add a signoff step to the `build` job. On `pull_request`, it checks every commit from `github.event.pull_request.base.sha` to `github.event.pull_request.head.sha` for a `Signed-off-by:` trailer. It skips the step when the PR author is `dependabot[bot]`. It fails with the commit hash in the message.
- [ ] 3.3 Run the signoff step locally against a branch with one signed and one unsigned commit. Verify that the step fails on the unsigned commit and passes on the signed one.

## 4. Settings and contributor guidance

- [ ] 4.1 In `.github/settings.yml`, remove the DCO check (app 1861) from the required checks of `main`. Keep `build`, `pre-commit.ci - pr` and `Semantic PR`.
- [ ] 4.2 In `CONTRIBUTING.adoc`, state that the `build` check requires a `Signed-off-by:` trailer on every commit. Remove the reference to the DCO certificate as an enforced check.
- [ ] 4.3 Verify that `.github/dependabot.yml` and `.github/autoapproval.yml` need no change for the signoff step.

## 5. Verification after merge

- [ ] 5.1 Confirm that the next release-please PR is authored by `github-actions`, shows all required checks, and that the `build` job skips it.
- [ ] 5.2 Confirm that a Dependabot PR passes `build`.
- [ ] 5.3 Confirm that a push to main that cuts no release runs the `snapshot` job and publishes a `-SNAPSHOT`.
- [ ] 5.4 Confirm that live branch protection on spock-deepmock lists `build`, `pre-commit.ci - pr` and `Semantic PR`, and no DCO check.
- [ ] 5.5 Close PR #243 once 5.1 is confirmed.
- [ ] 5.6 Delete the `RELEASE_PLEASE` secret from spock-deepmock.

## 6. Sibling repositories

- [ ] 6.1 percolate: copy `.github/semantic.yml` from spock-deepmock; add the `Semantic PR` check (app 198092) to `settings.yml`; verify the file is valid YAML.
- [ ] 6.2 percolate: add the signoff step to `build.yml` (task 3.2) and the `chore: release ` build condition (task 3.1).
- [ ] 6.3 pmd-rules: copy `.github/semantic.yml`; add `Semantic PR` (app 198092) and `build` checks to `settings.yml`; add the signoff step and the `chore: release ` condition to `build.yml`.
- [ ] 6.4 pmd-rules: add the `snapshot` job and the `workflow_dispatch` input from tasks 2.2 and 2.3 to `release.yml`.
- [ ] 6.5 percolate and pmd-rules: confirm that `RELEASE_PLEASE` is not referenced in any workflow; the default token is already used there.
- [ ] 6.6 pmd-rules: confirm that the Probot Settings App applies `settings.yml` to live branch protection, which is currently absent.

## 7. Final check

- [ ] 7.1 Run `./gradlew check` in spock-deepmock and confirm it passes. NEVER continue if there are violations.
