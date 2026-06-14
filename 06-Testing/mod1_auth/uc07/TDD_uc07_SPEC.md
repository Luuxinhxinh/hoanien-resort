# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC07 - PII Anonymization GDPR (PiiService)

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-TDD-MOD1-UC07-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyen Xuan Luu - Developer |
| **Reviewed by** | [x] Nguyen Xuan Luu - Tech Lead |
| **DPO Sign-off** | [x] Approved - 2026-06-14 |
| **Approved by** | [x] Nguyen Xuan Luu - 2026-06-14 |
| **Classification** | Internal - Confidential |

---

### 1. Thong tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | GAP-MOD1-UC07 |
| **Module** | Auth - UC07 |
| **Use Case** | UC07 - PII Anonymization GDPR |
| **Priority** | P0 |
| **Sprint** | S1 (2026-06-09 - 2026-06-23) |
| **Milestone** | M3 Alpha - 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream** | GDPR compliance |
| **Downstream** | AuditService |

### 2. Logic Issues Resolved
| L1 | Thieu validation logic | Them validate input | Test validation |

### 3. TDS

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC07-001 | Anonymize customer | `PiiService.anonymizeCustomer()` | TC-UC07-001 |
| TC-COND-UC07-002 | Audit trail after anonymization | `PiiService.getAnonymizationLog()` | TC-UC07-002 |
| TC-COND-UC07-003 | Cannot reverse anonymization | `PiiService.verifyAnonymized()` | TC-UC07-003 |

### 4. Test Case Specification

#### `TC-UC07-001` - Anonymize customer
* **Severity:** CRITICAL | **Feature:** `PiiService.anonymizeCustomer()` | **File:** `PiiServiceUC07Test.java` | GREEN
**Steps:** Test Anonymize customer functionality. Assert expected behavior.

#### `TC-UC07-002` - Audit trail after anonymization
* **Severity:** HIGH | **Feature:** `PiiService.getAnonymizationLog()` | **File:** `PiiServiceUC07Test.java` | GREEN
**Steps:** Test Audit trail after anonymization functionality. Assert expected behavior.

#### `TC-UC07-003` - Cannot reverse anonymization
* **Severity:** HIGH | **Feature:** `PiiService.verifyAnonymized()` | **File:** `PiiServiceUC07Test.java` | GREEN
**Steps:** Test Cannot reverse anonymization functionality. Assert expected behavior.

### 5. Red-Green-Refactor Tracker

| TC ID | Mocha | Test File | RED | GREEN | REFACTOR |
|---|---|---|---|---|---|
| TC-UC07-001 | Anonymize customer | `PiiServiceUC07Test.java` | [x] | [x] | [x] |
| TC-UC07-002 | Audit trail after anonymization | `PiiServiceUC07Test.java` | [x] | [x] | [x] |
| TC-UC07-003 | Cannot reverse anonymization | `PiiServiceUC07Test.java` | [x] | [x] | [x] |

### 6. Entry / Exit Criteria
- [x] Unit tests pass 100%

### 7. Rollback Plan
`git checkout -- src/main/java/com/kawai/services/impl/PiiServiceImpl.java`
