# Alocar

[![CI](https://github.com/Alocar-UA/Alocar/actions/workflows/ci.yml/badge.svg)](https://github.com/Alocar-UA/Alocar/actions/workflows/ci.yml)

## Project abstract

**Alocar** is a light-mobility fleet-as-a-service platform for residential condominiums
(TQS 2026 group project, *Service pools* concretization).

An operator supplies and maintains a shared fleet of e-bikes, cargo bikes and scooters for a
condominium. Residents book vehicles at the hub of their building for a time slot (or take one
right away) and return them to a hub. The condominium administration sets the rules of use
(maximum booking length, vehicle types allowed, hub capacity) and the **quotas per apartment
(fração)**, and follows KPIs. The core business rules are **overlapping bookings** and
**quotas per apartment**.

## Project team

| Role             | Member |
| ---------------- | ------ |
| Team Coordinator | _TBD_  |
| Product Owner    | _TBD_  |
| QA Engineer      | _TBD_  |
| DevOps Master    | _TBD_  |

## Project bookmarks

- Jira backlog: _TBD_
- Related repositories: none (single repository)
- API documentation (OpenAPI / Swagger UI): local http://localhost:8080/swagger-ui.html, deployed _TBD_
- Static analysis dashboard (SonarCloud): _TBD_
- Observability dashboard: _TBD_
- Deployed application: _TBD_
- Reports ([`docs/`](docs/README.md)):
  - Product Specification: [PDF](docs/reports/Alocar-Product-Specs.pdf) · [DOCX](docs/reports/Alocar-Product-Specs.docx)
  - QA Manual v1: [PDF](docs/reports/Alocar-QA-Manual-v1.pdf) · [DOCX](docs/reports/Alocar-QA-Manual-v1.docx)
  - Diagrams: [domain classes](docs/diagrams/class-diagram.png) · [scooter states](docs/diagrams/scooter-state-diagram.png)

## Repository layout

| Path        | Content                                                     |
| ----------- | ----------------------------------------------------------- |
| `backend/`  | Spring Boot 4 / Java 21 REST API (Maven)                    |
| `frontend/` | React + Vite + TypeScript web UI                            |
| `docs/`     | Our reports (`reports/`), diagrams (`diagrams/`), course material |
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

# Backend only: unit tests (Surefire) + integration tests with Testcontainers (Failsafe) + JaCoCo
cd backend && ./mvnw verify

# Frontend only
cd frontend && npm ci && npm run lint && npm test && npm run dev
```

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the workflow and [`AGENTS.md`](AGENTS.md) for AI agent instructions.
