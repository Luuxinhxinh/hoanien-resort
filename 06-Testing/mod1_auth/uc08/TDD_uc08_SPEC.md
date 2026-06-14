# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC08 - Price and Category Config (PriceConfigService)

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-TDD-MOD1-UC08-001 |
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
| **Feature / Gap ID** | GAP-MOD1-UC08 |
| **Module** | Auth - UC08 |
| **Use Case** | UC08 - Price and Category Config |
| **Priority** | P1 |
| **Sprint** | S1 (2026-06-09 - 2026-06-23) |
| **Milestone** | M3 Alpha - 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream** | UC04 (Master Data) |
| **Downstream** | UC09,UC10 (Booking) |

### 2. Logic Issues Resolved
| L1 | Thieu validation logic | Them validate input | Test validation |

### 3. TDS

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC08-001 | Create price config | `PriceConfigService.createPriceConfig()` | TC-UC08-001 |
| TC-COND-UC08-002 | Update price config | `PriceConfigService.updatePriceConfig()` | TC-UC08-002 |
| TC-COND-UC08-003 | Seasonal pricing | `PriceConfigService.applySeasonalPrice()` | TC-UC08-003 |
| TC-COND-UC08-004 | Invalid price range | `PriceConfigService.validatePrice()` | TC-UC08-004 |

### 4. Test Case Specification

#### `TC-UC08-001` - Create price config
* **Severity:** HIGH | **Feature:** `PriceConfigService.createPriceConfig()` | **File:** `PriceConfigServiceUC08Test.java` | GREEN
**Steps:** Test Create price config functionality. Assert expected behavior.

#### `TC-UC08-002` - Update price config
* **Severity:** HIGH | **Feature:** `PriceConfigService.updatePriceConfig()` | **File:** `PriceConfigServiceUC08Test.java` | GREEN
**Steps:** Test Update price config functionality. Assert expected behavior.

#### `TC-UC08-003` - Seasonal pricing
* **Severity:** MEDIUM | **Feature:** `PriceConfigService.applySeasonalPrice()` | **File:** `PriceConfigServiceUC08Test.java` | GREEN
**Steps:** Test Seasonal pricing functionality. Assert expected behavior.

#### `TC-UC08-004` - Invalid price range
* **Severity:** MEDIUM | **Feature:** `PriceConfigService.validatePrice()` | **File:** `PriceConfigServiceUC08Test.java` | GREEN
**Steps:** Test Invalid price range functionality. Assert expected behavior.

### 5. Red-Green-Refactor Tracker

| TC ID | Mocha | Test File | RED | GREEN | REFACTOR |
|---|---|---|---|---|---|
| TC-UC08-001 | Create price config | `PriceConfigServiceUC08Test.java` | [x] | [x] | [x] |
| TC-UC08-002 | Update price config | `PriceConfigServiceUC08Test.java` | [x] | [x] | [x] |
| TC-UC08-003 | Seasonal pricing | `PriceConfigServiceUC08Test.java` | [x] | [x] | [x] |
| TC-UC08-004 | Invalid price range | `PriceConfigServiceUC08Test.java` | [x] | [x] | [x] |

### 6. Entry / Exit Criteria
- [x] Unit tests pass 100%

### 7. Rollback Plan
`git checkout -- src/main/java/com/kawai/services/impl/PriceConfigServiceImpl.java`
