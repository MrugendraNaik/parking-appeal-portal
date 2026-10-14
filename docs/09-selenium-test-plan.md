# Experiment 9 — Selenium Test Plan

## Scope
Browser-level tests of the five critical user journeys, run with Selenium WebDriver 4 + JUnit 5 through the Maven Failsafe plugin. Unit and service tests (JUnit + Spring MockMvc) are run separately by Surefire.

## Environment
| Item | Value |
|---|---|
| Browser | Google Chrome (headless by default), Firefox optional `-Dbrowser=firefox` |
| Driver | Downloaded automatically by Selenium Manager |
| App under test | Started automatically on port 8095 by `mvn verify -Pselenium`, or any URL via `-DbaseUrl=... -DskipAppStart=true` |
| Test data | Each test calls `POST /api/test/violations` to create an unused ticket, so the suite can be rerun against the same server. Fixed strings live in `TestJourneyData.java`; evidence is a generated minimal PDF. |
| Reports | `target/failsafe-reports/*.xml` (JUnit format, published by Jenkins) |
| Failure screenshots | `target/screenshots/<Class>_<method>_<time>.png` via `ScreenshotOnFailureExtension` |

## Test cases

| ID | Class | Journey | Steps | Expected result |
|---|---|---|---|---|
| TC-01 | `J1_SubmitAppealIT` | Submit valid appeal | Home → File an appeal → fill all fields → attach PDF → Submit | Reference matches `APL-\d{8}-XXXX`, status Submitted |
| TC-02 | `J2_ValidationIT.invalidAppealShowsErrors` | Validation | Submit with 12-char reason, no file | Error banner; reason and evidence errors shown; no reference |
| TC-03 | `J2_ValidationIT.vehicleMismatchIsRejected` | Validation | Real ticket, wrong vehicle number | "does not match" under vehicle field |
| TC-04 | `J3_TrackStatusIT` | Track | Submit → Track with ref + email; then wrong email | Status Submitted; then "No appeal matches" |
| TC-05 | `J4_ReviewerApproveIT` | Approve | Submit → reviewer login → open → remarks → Approve → citizen tracks | Detail shows Under review before decision; citizen sees Approved + remarks |
| TC-06 | `J5_ReviewerRejectIT` | Reject | Submit → reviewer opens → Reject without remarks → add remarks → Reject | First attempt refused; then status Rejected and decision form gone |

## Entry / exit criteria
- **Entry:** build compiles; app health is UP.
- **Exit:** all six cases pass. Any failure fails the Maven build and (in Jenkins) stops deployment.

## Run commands
```bash
mvn verify -Pselenium                         # headless Chrome, app auto-started
mvn verify -Pselenium -Dheadless=false        # watch the browser
mvn verify -Pselenium -DskipAppStart=true -DbaseUrl=http://localhost:8081/parking-appeal   # against Tomcat
```
