# Alocar

[![CI](https://github.com/Alocar-UA/Alocar/actions/workflows/ci.yml/badge.svg)](https://github.com/Alocar-UA/Alocar/actions/workflows/ci.yml)

## Project abstract

**Alocar** is a shared electric-scooter service for residents of residences and condominiums
(TQS 2026 group project, *Service pools* concretization), run as a franchise network with one
franchise per city.

Each client residence (a *subfleet*) has a hub with scooters. A resident signs up through the
residence's link, picks a free scooter at the hub (no advance booking), chooses how long they will
use it and pays that time up front, then returns it to the same hub. Returning it 10 minutes late or
more costs a fine that grows with the delay. Scooters send their location and speed every 5 seconds,
used to confirm returns and to detect possible theft. The franchisee pays the residence owner, as
with a vending machine.

The product specification (stories US01–US10, business rules BR1–BR12) is in the Product Specs
report; [`docs/traceability.md`](docs/traceability.md) maps the rules to their tests.

## Project team

| Role             | Member |
| ---------------- | ------ |
| Team Coordinator | _TBD_  |
| Product Owner    | _TBD_  |
| QA Engineer      | _TBD_  |
| DevOps Master    | _TBD_  |

## Project bookmarks

- Jira backlog: project `SCRUM` (Xray test plan in the repository variable `XRAY_TEST_PLAN_KEY`)
- Related repositories: none (single repository)
- API documentation (OpenAPI / Swagger UI): local http://localhost:8080/swagger-ui.html, deployed _TBD_
- Static analysis dashboard (SonarCloud): _TBD_
- Observability dashboard: _TBD_
- Deployed application: _TBD_
- Reports: [`docs/`](docs/)

## Repository layout

| Path        | Content                                                     |
| ----------- | ----------------------------------------------------------- |
| `backend/`  | Spring Boot 4 / Java 21 REST API (Maven)                    |
| `frontend/` | React + Vite + TypeScript web UI                            |
| `docs/`     | Assignment guidelines, report templates, our reports and the requirements × tests traceability |
| `.github/`  | CI workflow, Dependabot, pull request template              |

## Running locally

Required tools and minimum versions are listed in [`requirements.txt`](requirements.txt).
Check them, and install what is missing, with:

```bash
./scripts/setup-dev.sh          # add --check to only check
```

```bash
# Whole stack (PostgreSQL + backend + frontend) with Docker Compose
cp .env.example .env   # then set DB_PASSWORD
docker compose up --build
# UI: http://localhost   API docs: http://localhost:8080/swagger-ui.html   Health: http://localhost:8080/actuator/health

# Backend only: unit tests (Surefire) + integration and Gherkin acceptance tests with
# Testcontainers (Failsafe) + JaCoCo. Cucumber reports: backend/target/cucumber-reports/
cd backend && ./mvnw verify

# Frontend only
cd frontend && npm ci && npm run lint && npm test && npm run dev
```

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the workflow and [`AGENTS.md`](AGENTS.md) for AI agent instructions.
