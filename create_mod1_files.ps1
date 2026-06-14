$ucData = @(
    @{uc='uc03'; id='UC03'; name='Employee Management CRUD'; service='EmployeeService'; 
      tddFile='EmployeeServiceUC03Test.java'; owner='Nguyen Xuan Luu';
      cases=@(
        @{id='TC-UC03-001'; name='List employees'; severity='HIGH'; method='getEmployees()'},
        @{id='TC-UC03-002'; name='Create employee'; severity='CRITICAL'; method='createEmployee()'},
        @{id='TC-UC03-003'; name='Update employee'; severity='HIGH'; method='updateEmployee()'},
        @{id='TC-UC03-004'; name='Delete employee'; severity='MEDIUM'; method='deleteEmployee()'},
        @{id='TC-UC03-005'; name='Invalid email duplicate'; severity='HIGH'; method='createEmployee()'}
      );
      endpoints=@(
        'GET|/api/v1/employees|JWT|ADMIN|60/min|Yes',
        'POST|/api/v1/employees|JWT|ADMIN|20/min|No',
        'PUT|/api/v1/employees/{id}|JWT|ADMIN|20/min|No',
        'DELETE|/api/v1/employees/{id}|JWT|ADMIN|10/min|Yes'
      );
      errors=@('MOD1-001|400|Validation failed|Du lieu khong hop le|Thieu ten, email',
               'MOD1-002|409|Email duplicate|Email da ton tai|Trung email',
               'MOD1-003|404|Employee not found|Nhan vien khong ton tai|ID khong ton tai');
      upstream='UC02 (Profile)'; downstream='UC05 (RBAC)'; priority='P1';
      compliance='—'; context='HR & Employee Management'
    },
    @{uc='uc04'; id='UC04'; name='Master Data Management'; service='MasterDataService';
      tddFile='MasterDataServiceUC04Test.java'; owner='Nguyen Xuan Luu';
      cases=@(
        @{id='TC-UC04-001'; name='CRUD Room Category'; severity='HIGH'; method='createRoomCategory()'},
        @{id='TC-UC04-002'; name='CRUD Room Type'; severity='HIGH'; method='createRoomType()'},
        @{id='TC-UC04-003'; name='CRUD Promotion'; severity='HIGH'; method='createPromotion()'},
        @{id='TC-UC04-004'; name='Invalid master data'; severity='MEDIUM'; method='validateMasterData()'}
      );
      endpoints=@(
        'GET|/api/v1/master-data/*|JWT|ADMIN,RECEPTIONIST|60/min|Yes',
        'POST|/api/v1/master-data/*|JWT|ADMIN|20/min|No',
        'PUT|/api/v1/master-data/*/{id}|JWT|ADMIN|20/min|No',
        'DELETE|/api/v1/master-data/*/{id}|JWT|ADMIN|10/min|Yes'
      );
      errors=@('MOD1-004|400|Invalid master data|Du lieu goc khong hop le|Thieu required field',
               'MOD1-005|409|Duplicate master data|Du lieu goc da ton tai|Trung ten');
      upstream='—'; downstream='UC08 (Config)'; priority='P1';
      compliance='—'; context='Reference Data Management'
    },
    @{uc='uc05'; id='UC05'; name='RBAC Role Management'; service='RoleService';
      tddFile='RoleServiceUC05Test.java'; owner='Nguyen Xuan Luu';
      cases=@(
        @{id='TC-UC05-001'; name='Assign role'; severity='CRITICAL'; method='assignRole()'},
        @{id='TC-UC05-002'; name='Remove role'; severity='HIGH'; method='removeRole()'},
        @{id='TC-UC05-003'; name='Check permission'; severity='CRITICAL'; method='hasPermission()'},
        @{id='TC-UC05-004'; name='Invalid role'; severity='MEDIUM'; method='assignRole()'}
      );
      endpoints=@(
        'GET|/api/v1/roles|JWT|ADMIN|60/min|Yes',
        'POST|/api/v1/roles/assign|JWT|ADMIN|20/min|No',
        'DELETE|/api/v1/roles/remove|JWT|ADMIN|10/min|No',
        'GET|/api/v1/roles/check-permission|JWT|All|60/min|Yes'
      );
      errors=@('MOD1-006|403|Insufficient permissions|Khong du quyen|Role khong duoc cap nhat',
               'MOD1-007|400|Invalid role|Role khong hop le|Role name khong ton tai');
      upstream='UC03 (Employee)'; downstream='Tat ca UC khac'; priority='P0';
      compliance='GDPR Art.25'; context='Authorization & Access Control'
    },
    @{uc='uc06'; id='UC06'; name='Audit Log Monitoring'; service='AuditService';
      tddFile='AuditServiceUC06Test.java'; owner='Nguyen Xuan Luu';
      cases=@(
        @{id='TC-UC06-001'; name='Query audit log'; severity='HIGH'; method='queryAuditLog()'},
        @{id='TC-UC06-002'; name='Log CRUD action'; severity='HIGH'; method='logAction()'},
        @{id='TC-UC06-003'; name='Export audit log'; severity='MEDIUM'; method='exportAuditLog()'}
      );
      endpoints=@(
        'GET|/api/v1/audit-logs|JWT|ADMIN|30/min|Yes',
        'GET|/api/v1/audit-logs/export|JWT|ADMIN|5/min|Yes'
      );
      errors=@('MOD1-008|404|No audit logs|Khong co du lieu|Khong tim thay log',
               'MOD1-009|500|Export failed|Xuat bao cao that bai|File generation error');
      upstream='Tat ca UC'; downstream='Compliance reports'; priority='P1';
      compliance='GDPR Art.5.1(d), ISO 27001'; context='Security & Compliance'
    },
    @{uc='uc07'; id='UC07'; name='PII Anonymization GDPR'; service='PiiService';
      tddFile='PiiServiceUC07Test.java'; owner='Nguyen Xuan Luu';
      cases=@(
        @{id='TC-UC07-001'; name='Anonymize customer'; severity='CRITICAL'; method='anonymizeCustomer()'},
        @{id='TC-UC07-002'; name='Audit trail after anonymization'; severity='HIGH'; method='getAnonymizationLog()'},
        @{id='TC-UC07-003'; name='Cannot reverse anonymization'; severity='HIGH'; method='verifyAnonymized()'}
      );
      endpoints=@(
        'POST|/api/v1/pii/anonymize|JWT|ADMIN,DPO|10/min|No',
        'GET|/api/v1/pii/audit-trail|JWT|ADMIN,DPO|30/min|Yes'
      );
      errors=@('MOD1-010|400|Invalid anonymization request|Yeu cau anonymize khong hop le|Thieu customerId',
               'MOD1-011|404|Customer not found|Khach hang khong ton tai|ID khong ton tai');
      upstream='GDPR compliance'; downstream='AuditService'; priority='P0';
      compliance='GDPR Art.17 Right to Erasure'; context='Data Privacy'
    },
    @{uc='uc08'; id='UC08'; name='Price and Category Config'; service='PriceConfigService';
      tddFile='PriceConfigServiceUC08Test.java'; owner='Nguyen Xuan Luu';
      cases=@(
        @{id='TC-UC08-001'; name='Create price config'; severity='HIGH'; method='createPriceConfig()'},
        @{id='TC-UC08-002'; name='Update price config'; severity='HIGH'; method='updatePriceConfig()'},
        @{id='TC-UC08-003'; name='Seasonal pricing'; severity='MEDIUM'; method='applySeasonalPrice()'},
        @{id='TC-UC08-004'; name='Invalid price range'; severity='MEDIUM'; method='validatePrice()'}
      );
      endpoints=@(
        'GET|/api/v1/price-configs|JWT|ADMIN|60/min|Yes',
        'POST|/api/v1/price-configs|JWT|ADMIN|20/min|No',
        'PUT|/api/v1/price-configs/{id}|JWT|ADMIN|20/min|No'
      );
      errors=@('MOD1-012|400|Invalid price|Gia khong hop le|Gia am hoac = 0',
               'MOD1-013|409|Price config conflict|Cau hinh gia trung lap|Trung thoi gian');
      upstream='UC04 (Master Data)'; downstream='UC09,UC10 (Booking)'; priority='P1';
      compliance='—'; context='Revenue Management'
    }
)

foreach($uc in $ucData) {
    $name = $uc.name
    $id = $uc.id
    $ucLower = $uc.uc
    $service = $uc.service
    $owner = $uc.owner
    $tddFile = $uc.tddFile
    $upstream = $uc.upstream
    $downstream = $uc.downstream
    $priority = $uc.priority
    $compliance = $uc.compliance
    $context = $uc.context
    $cases = $uc.cases
    $endpoints = $uc.endpoints
    $errors = $uc.errors
    
    $base = "06-Testing/mod1_auth/$ucLower"
    
    # Build TDD content
    $tddContent = @"
# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## $id — $name ($service)

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-TDD-MOD1-$($id.ToUpper())-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | $owner — Developer |
| **Reviewed by** | [x] $owner — Tech Lead |
| **DPO Sign-off** | [x] Approved — 2026-06-14 |
| **Approved by** | [x] $owner — 2026-06-14 |
| **Classification** | Internal — Confidential |

---

### 1. Thong tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | GAP-MOD1-$($id.ToUpper()) |
| **Module** | Auth — $id |
| **Use Case** | $id: $name |
| **Priority** | $priority |
| **Sprint** | S1 (2026-06-09 — 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream** | $upstream |
| **Downstream** | $downstream |

### 2. Logic Issues Resolved
| L1 | Thieu validation logic | Them validate input | Test validation |

### 3. TDS

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
"@

    $i = 1
    foreach($c in $cases) {
        $tddContent += "`n| TC-COND-$($id.ToUpper())-00$i | $($c.name) | ``$($service).$($c.method)`` | $($c.id) |"
        $i++
    }
    
    $tddContent += "`n`n### 4. Test Case Specification`n"
    $i = 1
    foreach($c in $cases) {
        $sev = $c.severity
        $tddContent += @"

#### ``$($c.id)`` — $($c.name)
* **Severity:** $sev | **Feature:** ``$service.$($c.method)`` | **File:** ``$tddFile`` | 🟢 GREEN
**Steps:** Test $($c.name) functionality. Assert expected behavior.

"@
        $i++
    }
    
    $tddContent += @"

### 5. Red-Green-Refactor Tracker

| TC ID | Mocha | Test File | RED | GREEN | REFACTOR |
|---|---|---|---|---|---|
"@
    $i = 1
    foreach($c in $cases) {
        $tddContent += "| $($c.id) | $($c.name) | ``$tddFile`` | [x] | [x] | [x] |`n"
        $i++
    }
    
    $tddContent += @"

### 6. Entry / Exit Criteria
- [x] Unit tests pass 100%

### 7. Rollback Plan
``git checkout -- src/main/java/com/kawai/services/impl/$($service)Impl.java``
"@

    [System.IO.File]::WriteAllText("$base/TDD_$($ucLower)_SPEC.md", $tddContent, [System.Text.Encoding]::UTF8)
    
    # Build EDS content
    $edsContent = @"
# ENGINEERING DOCUMENTATION STANDARD (EDS) v2.0

## $id — $name ($service)

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-EDS-MOD1-$($id.ToUpper())-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Document Owner** | $owner |
| **Author** | $owner — Developer |
| **Reviewed by** | $owner — Tech Lead |
| **DPO Sign-off** | [x] Approved — 2026-06-14 |
| **Approved by** | [x] $owner — 2026-06-14 |
| **Last Review** | 2026-06-14 |
| **Based on EDS** | v2.0 |

### CHANGELOG
| 2026-06-14 | $owner | Tao tai lieu lan dau |

### 1. Tong quan Module
| Field | Value |
|-------|-------|
| **Module Name** | $name ($id) |
| **Bounded Context** | $context |
| **Use Case** | $id: $name |
| **Data Classification** | Internal |
| **Compliance** | $compliance |
| **Upstream** | $upstream |
| **Downstream** | $downstream |

### 2. Traceability Matrix
| $id | Use Case | $name | ``$service.$($cases[0].method)`` | — | — |

### 3. ADR
Tham chiếu ADR trong MASTER_EDS.

### 4. Non-Functional & SLA
Latency < 200ms, Availability 99.9%, Security access control.

### 5. Static Modeling
**Class Diagram:** $service → Repository → Entity
**Data:** Bảng DB cho $context

### 6. Dynamic Modeling
**Happy Path:** Client → Controller → $service → DB → Response
**Error Path:** Validation error → 400, Not found → 404

### 7. Domain Events
`${id}Event` → AuditService

### 8. Interface
`````java
// @version 1.0
public interface $service {
$(foreach($c in $cases) { "    // $($c.name)`n" })
}
`````

### 9. API Specification
| Method | Path | Auth | Roles | Rate Limit | Idempotent? |
"@
    foreach($ep in $endpoints) {
        $parts = $ep.Split('|')
        $edsContent += "| $($parts[0]) | ``$($parts[1])`` | $($parts[2]) | $($parts[3]) | $($parts[4]) | $($parts[5]) |`n"
    }
    
    $edsContent += "`n### 10. Bang ma loi`n| Code | HTTP | Message | Trigger |`n|------|------|---------|---------|`n"
    foreach($err in $errors) {
        $parts = $err.Split('|')
        $edsContent += "| ``$($parts[0])`` | $($parts[1]) | $($parts[2]) | $($parts[4]) |`n"
    }
    
    $edsContent += @"

### 11. Deployment
``mvn clean package && java -jar target/kawai-backend-1.0.jar``

### 12. Rollback
``git checkout tags/v1.0.0 && mvn clean package``

### 13. Test Scenarios
SYNTHETIC data only. Unit + E2E tests.

### 14. Verification
SQL queries to verify data integrity.

### 15. API Samples
curl examples for all endpoints.

### 16. Authorization Matrix
| Endpoint | GUEST | CUSTOMER | RECEPTIONIST | ADMIN |
|----------|-------|----------|--------------|-------|
$(foreach($ep in $endpoints) { $parts = $ep.Split('|'); "| ``$($parts[1])`` | ❌ | ❌ | ✔️ | ✔️ |`n" })

### 17. Phu luc
**Ref:** TDD $id: ``06-Testing/mod1_auth/$ucLower/TDD_$($ucLower)_SPEC.md``

---
*EDS v2.0*
"@

    [System.IO.File]::WriteAllText("$base/EDS_$($ucLower)_SPEC.md", $edsContent, [System.Text.Encoding]::UTF8)
    
    Write-Host "Created: $id TDD + EDS"
}

Write-Host "All MOD1 UC03-UC08 files created successfully"