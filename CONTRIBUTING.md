# Branch and Commit Policy

## Branches

| Branch | Purpose | Who merges |
|---|---|---|
| `main` | Released, tagged code only (`v1.0.0`, `v1.1.0` …) | Team lead, from `develop` |
| `develop` | Integration branch; Jenkins builds this | Via reviewed pull request |
| `feature/T##-short-name` | One backlog task, e.g. `feature/T05-appeal-submission` | Author opens PR → `develop` |
| `bugfix/short-name` | Fix found during development/testing | PR → `develop` |
| `hotfix/short-name` | Urgent fix to released code | PR → `main` and `develop` |

Rules: lowercase, hyphens, task ID in feature branch names. Never commit directly to `main` or `develop`. Delete the branch after merge.

## Commit messages

`<type>: <what changed>` in the imperative, ≤ 72 characters. Reference the issue when there is one.

| Type | Use for |
|---|---|
| `feat` | New functionality — `feat: add appeal submission form (#5)` |
| `fix` | Bug fix — `fix: approve button sent REJECT decision` |
| `test` | Tests — `test: add Selenium reviewer approval journey` |
| `ci` | Jenkins/Docker/Ansible — `ci: add Docker build and push stages` |
| `docs` | Documentation — `docs: add SRS summary` |
| `chore` | Build/config housekeeping — `chore: initial Maven skeleton` |

## Pull requests

Use the PR template, link the issue (`Closes #5`), get at least one review approval, and make sure Jenkins is green before merging.
