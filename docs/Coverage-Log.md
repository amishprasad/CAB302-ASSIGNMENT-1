# Test coverage log

Coverage is measured with JaCoCo (`mvn clean verify`, then open `target/site/jacoco/index.html`).

- **Before:** report taken without the three new test files (`UserDAOTest`, `ProfileDAOTest`, `PasswordUtilTest`).
- **After:** report taken with the new tests added.

Screenshots: `docs/img/coverage-before.png` and `docs/img/coverage-after.png`

## Results

| Measure | Before | After |
|---|---|---|
| `db` package, instructions | 55% | **85%** |
| `db` package, branches | 41% | **76%** |
| `db` package, lines covered | 313 of 522 (60%) | **440 of 522 (84%)** |
| `util` package, branches | 94% | 96% |
| Whole project, instructions | 25% | **28%** |
| Whole project, branches | 34% | **39%** |
| Whole project, lines covered | 1,020 of 4,446 (23%) | **1,165 of 4,446 (26%)** |

## What changed

41 new tests were added:

| Test class | Tests | Covers |
|---|---|---|
| `UserDAOTest` | 22 | Registration, duplicate username/email/phone rejected, login by username or email, wrong password, password change, lookups |
| `ProfileDAOTest` | 9 | Save and load round trip, updating instead of duplicating, per-user isolation |
| `PasswordUtilTest` | 10 | Hashing, random salt, wrong and malformed passwords |

## Findings from the assessment

- The `db` package holds login, registration and profile saving, but it was at 55% instruction coverage and 41% branch coverage before this change.
- The `view` package is about 70% of the codebase (3,100 of 4,446 lines) and sits at about 1% coverage. This is why overall coverage only moved from 23% to 26% of lines.
- `ProfileDAO` takes the global `LocalProfileStore` singleton (private constructor, public fields), so its tests have to reset shared state before and after every test. This is evidence for the refactor in Step E.
- The `session` package (`UserSession`, 4 lines) is at 0%.
- Remaining uncovered lines in the `db` package are mostly `catch (SQLException)` branches.

## Link to the change log

Add a row to `docs/change-log.md` pointing at this file and at the PR that added the tests.