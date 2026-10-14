# Experiment 1 — Problem Definition and Scope

## 1.1 Problem statement

When a motorist in Mumbai receives a parking challan they believe is wrong — the "No Parking" board was hidden, the vehicle number was misread, the car was actually inside a legal bay — the only way to contest it today is to visit a traffic office or court in person, carry printed photographs, and follow up repeatedly by phone. There is no reference number, no visibility of where the appeal stands, and no written reason when it is turned down. On the department side, appeals arrive as paper, are checked by hand against the ticket register, and have no audit trail.

The **Automated Parking Violation Appeal Portal** lets a citizen file an appeal online against a specific ticket, attach evidence, and track its status with a reference number, while a reviewer works through a queue and records an approve/reject decision with remarks. The software itself is built and released through a DevOps toolchain (Git → Jenkins → Selenium → Docker → Ansible) so that every change is tested and deployed the same way.

## 1.2 Target users

| User | What they need |
|---|---|
| Citizen / vehicle owner | File an appeal from a phone or laptop, upload proof, know the outcome |
| Reviewer (traffic officer) | One queue of pending appeals with the ticket details and evidence side-by-side; record a decision quickly |
| Traffic department admin | Counts by status, audit of who decided what and when |
| DevOps / project team | Repeatable build, test, deploy and rollback |

## 1.3 Existing pain points

1. Appeals need a physical visit — loss of a working day for the citizen.
2. No acknowledgement or reference number; status enquiries are by phone.
3. Evidence (photos) is printed and can get separated from the file.
4. Reviewers manually cross-check the ticket register; mismatched vehicle numbers and late appeals are caught late.
5. Rejections often carry no written reason.
6. No record of turnaround time, so the department cannot measure its backlog.

## 1.4 Stakeholders

| Stakeholder | Interest | Influence |
|---|---|---|
| Citizens (vehicle owners) | Fair, fast, transparent appeal | High (end users) |
| Traffic police reviewers | Less paperwork, clear evidence | High |
| Traffic department head | Backlog, turnaround, accountability | High (sponsor) |
| Municipal IT cell | Hosting, security, maintenance | Medium |
| Project team (dev, QA, DevOps) | Deliver MVP on time with working pipeline | Medium |
| Faculty guide / evaluator | Coverage of all DevOps practices | Medium |

## 1.5 Objectives

1. Let a citizen submit an appeal against an issued ticket with evidence in under 3 minutes.
2. Validate the appeal automatically (ticket exists, vehicle matches, within 30 days, valid file) before it reaches a reviewer.
3. Give every appeal a reference and a trackable status: Submitted → Under review → Approved/Rejected.
4. Make rejections accountable — remarks are mandatory.
5. Build, test and deploy every commit automatically, and be able to roll back a bad release.

## 1.6 Constraints

| Type | Constraint |
|---|---|
| Time | One semester mini-project; 4 one-week sprints |
| Team | Small student team; no dedicated ops staff |
| Technology | Java 17, Maven, Spring Boot, Tomcat 10.1 / Docker, Jenkins, Selenium, Ansible (as required by the lab) |
| Infrastructure | Laptops / a single Ubuntu VM; no paid cloud |
| Data | No access to the real challan database — a seeded ticket table stands in for it |
| Security | MVP uses a shared reviewer access key, not full user accounts |
| Files | Evidence limited to PDF/JPG/PNG, max 2 MB |

## 1.7 Measurable success criteria

| # | Criterion | Target |
|---|---|---|
| S1 | Valid appeal submitted and reference shown | 100% of valid test cases |
| S2 | Invalid ticket / vehicle mismatch / late / bad file blocked | 100% of negative test cases |
| S3 | Reviewer decision visible to the citizen | Immediately on refresh |
| S4 | Unit + Selenium tests run on every commit | Every Jenkins build |
| S5 | A failing test prevents deployment | Demonstrated once (Exp 10) |
| S6 | Commit-to-running-container time | < 10 minutes |
| S7 | Re-running Ansible changes nothing | `changed=0` on second run |
| S8 | Unhealthy release rolled back automatically | Service healthy on previous tag |

## 1.8 Approved MVP scope (frozen at 15 tasks)

**In scope:** appeal submission with evidence upload; document/data validation; reviewer login and queue; approve/reject with remarks; status tracking by reference + email; REST API; health endpoint; full CI/CD pipeline.

**Out of scope (future):** citizen accounts and OTP login, SMS/email notifications, online fine payment, integration with the real e-challan system, multiple reviewer roles and escalation, analytics dashboard.

| ID | Task | Sprint |
|---|---|---|
| T01 | Problem definition and scope | 1 |
| T02 | Backlog, user stories, DoD, DevOps workflow | 1 |
| T03 | SRS, architecture, data model, local setup | 1 |
| T04 | GitHub repository skeleton and branch policy | 1 |
| T05 | Appeal submission form + ticket lookup | 2 |
| T06 | Document/data validation rules | 2 |
| T07 | Reviewer login and appeals queue | 2 |
| T08 | Approve / reject with remarks | 2 |
| T09 | Status tracking page + REST API | 2 |
| T10 | Jenkins CI job (build + archive) | 3 |
| T11 | Jenkinsfile pipeline + Tomcat deployment | 3 |
| T12 | Selenium suite + continuous testing gate | 3 |
| T13 | Docker image and container lifecycle | 4 |
| T14 | Jenkins → Docker continuous deployment | 4 |
| T15 | Ansible provisioning, idempotency, health check, rollback | 4 |
