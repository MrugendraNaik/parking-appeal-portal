# Automated Parking Violation Appeal Portal

DevOps mini-project (23102C0049). A citizen files an appeal against a parking ticket with evidence, the system validates it, a reviewer approves or rejects it with remarks, and the citizen tracks the status — built, tested and released through **Git → Jenkins → Selenium → Docker → Ansible**.

| | |
|---|---|
| Stack | Java 17 · Spring Boot 3.3 · Thymeleaf · Spring Data JPA · H2 |
| Build | Maven → `target/parking-appeal.war` (runs in Tomcat 10.1 **or** `java -jar`) |
| Tests | JUnit 5 + MockMvc (unit), Selenium WebDriver 4 (5 UI journeys) |
| CI/CD | Jenkins declarative pipeline (`Jenkinsfile`) |
| Container | Multi-stage `Dockerfile`, non-root, health check |
| Config mgmt | Ansible (`ansible/`) — provision, deploy, auto-rollback |

## Quick start

```bash
mvn spring-boot:run            # http://localhost:8080
mvn test                       # unit tests
mvn verify -Pselenium          # Selenium UI tests (needs Google Chrome)
```

Demo ticket: **PV-100001** / vehicle **MH01AB1234**. Reviewer key: **reviewer123**.

👉 **Full step-by-step instructions for all 14 experiments: [RUN_GUIDE.md](RUN_GUIDE.md)**

## Repository layout

```
├── src/main/java/com/vit/parkingappeal
│   ├── model/        Violation, Appeal, AppealStatus, Decision
│   ├── repository/   Spring Data JPA repositories
│   ├── service/      AppealService (workflow), AppealValidator (validation), EvidenceStorage
│   ├── web/          Citizen, reviewer and REST controllers
│   └── config/       Seed data, reviewer login interceptor, health indicator
├── src/main/resources/templates   Thymeleaf pages
├── src/test/java/…/service, web   Unit + MockMvc tests  (mvn test)
├── src/test/java/…/selenium       Selenium journeys J1–J5 (mvn verify -Pselenium)
├── Jenkinsfile        CI/CD pipeline
├── Dockerfile         Container image
├── ansible/           Inventory + provision / deploy / rollback playbooks
├── docs/              Experiment deliverables (scope, agile plan, SRS, test plan, config spec)
└── .github/           Issue + PR templates
```

## Branch policy

See [CONTRIBUTING.md](CONTRIBUTING.md): `main` (released) ← `develop` (integration) ← `feature/T##-short-name`.

## Configuration

| Env variable | Default | Purpose |
|---|---|---|
| `APP_ENV` | `dev` | Environment name shown in the footer and health |
| `REVIEWER_KEY` | `reviewer123` | Reviewer access key |
| `APP_DATA_DIR` | `./data` | H2 database + uploaded evidence |
| `APP_TEST_SUPPORT` | `false` | Enables the test-data endpoint used by Selenium |
| `APP_SIMULATE_FAILURE` | `false` | Makes `/actuator/health` report DOWN (rollback drill) |
| `PORT` | `8080` | HTTP port |
