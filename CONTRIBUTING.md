# Contributing to Alocar

## Workflow (GitHub flow)

1. Pick a story in Jira and move it to *In Progress*.
2. Create a branch from `main` named after the story: `SCRUM-12-start-use`.
3. Commit in small steps; every commit message starts with the Jira key: `SCRUM-12 Refuse a second active use`.
   With TDD, commit the failing test first (`SCRUM-12 test: ...`), then the implementation.
4. Open a pull request whose title starts with the Jira key and fill in the template.
5. CI must be green (build, tests, SonarQube quality gate) and one other team member must approve.
6. Merge with a merge commit (keeps the test-first commits as TDD evidence); the branch is deleted.

`main` is protected: no direct pushes, merges only through pull requests with passing checks
(`backend`, `frontend`, `smoke-test`, `dependency-scan`) and one approval.

## Code style

- **Java**: [Google Java Style](https://google.github.io/styleguide/javaguide.html) with 4-space
  indentation. Constructor injection only. DTOs as `record`s. No business logic in controllers.
- **TypeScript/React**: ESLint + Prettier as configured in `frontend/` (`npm run lint`, `npm run format`).
  Function components and hooks only.
- Names, code and comments in English; UI text in Portuguese.
- Database changes only through new Flyway migrations.

## Tests

| Kind        | Where / naming                     | When it runs              |
| ----------- | ---------------------------------- | ------------------------- |
| Unit        | `backend/src/test`, `*Test`        | `./mvnw test`, every PR    |
| Integration | `backend/src/test`, `*IT`          | `./mvnw verify`, every PR    |
| Frontend    | `frontend/src/**/*.test.tsx`       | `npm test`, every PR    |
| Acceptance  | `backend/src/test/resources/features/*.feature` (Gherkin, run by `CucumberIT`) | `./mvnw verify`, every PR    |
| E2E / load  | added in later iterations          | not on every build        |

## Review checklist

- [ ] The change does what the story's acceptance criteria say, and nothing else.
- [ ] Business rules have unit tests with boundary values; endpoints have integration tests.
- [ ] No duplicated logic, dead code or leftover debug output.
- [ ] Errors are handled and return meaningful HTTP status codes.
- [ ] No secrets, credentials or personal data.
- [ ] AI review findings were read; accepted/rejected ones are recorded in the PR description.

## AI use

Follow [`AGENTS.md`](AGENTS.md) and the team AI policy in the QA Manual.
