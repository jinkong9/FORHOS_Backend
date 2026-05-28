# FORHOS Service Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the next FORHOS backend maturity layer: admin member assignment, smarter wait estimates, no-show handling, member medical profiles, and symptom-based department recommendations.

**Architecture:** Keep the existing Spring Boot MVC/JPA style. Add focused domain entities and controllers, reuse existing `MemberService`, `HospitalService`, and `ReceptionService` where behavior belongs, and keep authorization checks in service methods plus `SecurityConfig`.

**Tech Stack:** Java 21, Spring Boot 4, Spring MVC, Spring Security, Spring Data JPA, H2 tests, MySQL runtime.

---

### Task 1: Admin Member Management

**Files:**
- Create: `src/main/java/com/jin/practice/admin/Controller/AdminMemberController.java`
- Create: `src/main/java/com/jin/practice/admin/dto/HospitalAdminCreateDto.java`
- Create: `src/main/java/com/jin/practice/admin/dto/MemberHospitalAssignDto.java`
- Create: `src/main/java/com/jin/practice/admin/dto/MemberRoleUpdateDto.java`
- Modify: `src/main/java/com/jin/practice/member/service/MemberService.java`
- Test: `src/test/java/com/jin/practice/member/service/MemberServiceAdminTest.java`

- [ ] Write failing service tests for creating a hospital admin, assigning a hospital, and updating a member role.
- [ ] Add DTOs and controller endpoints under `/api/admin/members`.
- [ ] Implement `MemberService` methods with duplicate email/phone checks and hospital lookup.
- [ ] Verify with targeted tests, then full test suite.

### Task 2: Dynamic Wait Estimates

**Files:**
- Modify: `src/main/java/com/jin/practice/reception/Repository/ReceptionRepository.java`
- Modify: `src/main/java/com/jin/practice/reception/service/ReceptionService.java`
- Test: `src/test/java/com/jin/practice/reception/service/ReceptionServiceTest.java`

- [ ] Write failing tests proving completed receptions determine average minutes per waiting patient.
- [ ] Add repository query for recent completed receptions by hospital.
- [ ] Update waiting stat calculation to use average completed duration with a 10-minute fallback.
- [ ] Verify create/call/complete/cancel all refresh waiting stats.

### Task 3: No-Show Handling

**Files:**
- Modify: `src/main/java/com/jin/practice/reception/entity/QueueStatus.java`
- Modify: `src/main/java/com/jin/practice/reception/entity/Reception.java`
- Modify: `src/main/java/com/jin/practice/reception/service/ReceptionService.java`
- Modify: `src/main/java/com/jin/practice/reception/Controller/AdminReceptionController.java`
- Test: `src/test/java/com/jin/practice/reception/service/ReceptionServiceTest.java`

- [ ] Write failing tests for marking only called receptions as no-show.
- [ ] Add `NO_SHOW` status and timestamp.
- [ ] Add `/api/admin/receptions/{receptionId}/no-show`.
- [ ] Exclude no-show from active queue counts.

### Task 4: Member Medical Profile

**Files:**
- Create: `src/main/java/com/jin/practice/member/entity/MemberMedicalProfile.java`
- Create: `src/main/java/com/jin/practice/member/dto/MedicalProfileDto.java`
- Modify: `src/main/java/com/jin/practice/member/entity/Member.java`
- Modify: `src/main/java/com/jin/practice/member/service/MemberService.java`
- Modify: `src/main/java/com/jin/practice/member/Controller/MemberController.java`
- Test: `src/test/java/com/jin/practice/member/service/MemberServiceMedicalProfileTest.java`

- [ ] Write failing tests for creating/updating and reading the authenticated member's medical profile.
- [ ] Add embedded one-to-one profile data owned by `Member`.
- [ ] Add `GET/PATCH /api/members/medical-profile`.
- [ ] Keep profile optional and return empty strings when not set.

### Task 5: Symptom Department Recommendation

**Files:**
- Create: `src/main/java/com/jin/practice/recommendation/Controller/SymptomRecommendationController.java`
- Create: `src/main/java/com/jin/practice/recommendation/dto/SymptomRecommendationRequestDto.java`
- Create: `src/main/java/com/jin/practice/recommendation/dto/SymptomRecommendationDto.java`
- Create: `src/main/java/com/jin/practice/recommendation/service/SymptomRecommendationService.java`
- Test: `src/test/java/com/jin/practice/recommendation/service/SymptomRecommendationServiceTest.java`

- [ ] Write failing tests for keyword matches and fallback to general medicine.
- [ ] Implement a deterministic rule-based mapper.
- [ ] Add `POST /api/recommendations/departments`.
- [ ] Document the endpoint in README.

### Task 6: Review and Verification

- [ ] Run `.\gradlew.bat clean test` with Java 21.
- [ ] Run `git diff --check`.
- [ ] Perform gstack-style review for auth boundaries, state transitions, data consistency, and test gaps.
- [ ] Fix all blocking findings.
- [ ] Commit and push only after fresh verification passes.
