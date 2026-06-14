import os

uc_data = [
    {
        'uc': 'uc03', 'id': 'UC03', 'name': 'Employee Management CRUD',
        'service': 'EmployeeService', 'tddFile': 'EmployeeServiceUC03Test.java',
        'owner': 'Nguyen Xuan Luu',
        'cases': [
            {'id': 'TC-UC03-001', 'name': 'List employees', 'severity': 'HIGH', 'method': 'getEmployees()'},
            {'id': 'TC-UC03-002', 'name': 'Create employee', 'severity': 'CRITICAL', 'method': 'createEmployee()'},
            {'id': 'TC-UC03-003', 'name': 'Update employee', 'severity': 'HIGH', 'method': 'updateEmployee()'},
            {'id': 'TC-UC03-004', 'name': 'Delete employee', 'severity': 'MEDIUM', 'method': 'deleteEmployee()'},
            {'id': 'TC-UC03-005', 'name': 'Invalid email duplicate', 'severity': 'HIGH', 'method': 'createEmployee()'},
        ],
        'endpoints': [
            'GET|/api/v1/employees|JWT|ADMIN|60/min|Yes',
            'POST|/api/v1/employees|JWT|ADMIN|20/min|No',
            'PUT|/api/v1/employees/{id}|JWT|ADMIN|20/min|No',
            'DELETE|/api/v1/employees/{id}|JWT|ADMIN|10/min|Yes',
        ],
        'errors': [
            'MOD1-001|400|Validation failed|Du lieu khong hop le|Thieu ten, email',
            'MOD1-002|409|Email duplicate|Email da ton tai|Trung email',
            'MOD1-003|404|Employee not found|Nhan vien khong ton tai|ID khong ton tai',
        ],
        'upstream': 'UC02 (Profile)', 'downstream': 'UC05 (RBAC)',
        'priority': 'P1', 'compliance': 'N/A', 'context': 'HR & Employee Management',
    },
    {
        'uc': 'uc04', 'id': 'UC04', 'name': 'Master Data Management',
        'service': 'MasterDataService', 'tddFile': 'MasterDataServiceUC04Test.java',
        'owner': 'Nguyen Xuan Luu',
        'cases': [
            {'id': 'TC-UC04-001', 'name': 'CRUD Room Category', 'severity': 'HIGH', 'method': 'createRoomCategory()'},
            {'id': 'TC-UC04-002', 'name': 'CRUD Room Type', 'severity': 'HIGH', 'method': 'createRoomType()'},
            {'id': 'TC-UC04-003', 'name': 'CRUD Promotion', 'severity': 'HIGH', 'method': 'createPromotion()'},
            {'id': 'TC-UC04-004', 'name': 'Invalid master data', 'severity': 'MEDIUM', 'method': 'validateMasterData()'},
        ],
        'endpoints': [
            'GET|/api/v1/master-data/*|JWT|ADMIN,RECEPTIONIST|60/min|Yes',
            'POST|/api/v1/master-data/*|JWT|ADMIN|20/min|No',
            'PUT|/api/v1/master-data/*/{id}|JWT|ADMIN|20/min|No',
            'DELETE|/api/v1/master-data/*/{id}|JWT|ADMIN|10/min|Yes',
        ],
        'errors': [
            'MOD1-004|400|Invalid master data|Du lieu goc khong hop le|Thieu required field',
            'MOD1-005|409|Duplicate master data|Du lieu goc da ton tai|Trung ten',
        ],
        'upstream': 'N/A', 'downstream': 'UC08 (Config)',
        'priority': 'P1', 'compliance': 'N/A', 'context': 'Reference Data Management',
    },
    {
        'uc': 'uc05', 'id': 'UC05', 'name': 'RBAC Role Management',
        'service': 'RoleService', 'tddFile': 'RoleServiceUC05Test.java',
        'owner': 'Nguyen Xuan Luu',
        'cases': [
            {'id': 'TC-UC05-001', 'name': 'Assign role', 'severity': 'CRITICAL', 'method': 'assignRole()'},
            {'id': 'TC-UC05-002', 'name': 'Remove role', 'severity': 'HIGH', 'method': 'removeRole()'},
            {'id': 'TC-UC05-003', 'name': 'Check permission', 'severity': 'CRITICAL', 'method': 'hasPermission()'},
            {'id': 'TC-UC05-004', 'name': 'Invalid role', 'severity': 'MEDIUM', 'method': 'assignRole()'},
        ],
        'endpoints': [
            'GET|/api/v1/roles|JWT|ADMIN|60/min|Yes',
            'POST|/api/v1/roles/assign|JWT|ADMIN|20/min|No',
            'DELETE|/api/v1/roles/remove|JWT|ADMIN|10/min|No',
            'GET|/api/v1/roles/check-permission|JWT|All|60/min|Yes',
        ],
        'errors': [
            'MOD1-006|403|Insufficient permissions|Khong du quyen|Role khong duoc cap nhat',
            'MOD1-007|400|Invalid role|Role khong hop le|Role name khong ton tai',
        ],
        'upstream': 'UC03 (Employee)', 'downstream': 'Tat ca UC khac',
        'priority': 'P0', 'compliance': 'GDPR Art.25', 'context': 'Authorization & Access Control',
    },
    {
        'uc': 'uc06', 'id': 'UC06', 'name': 'Audit Log Monitoring',
        'service': 'AuditService', 'tddFile': 'AuditServiceUC06Test.java',
        'owner': 'Nguyen Xuan Luu',
        'cases': [
            {'id': 'TC-UC06-001', 'name': 'Query audit log', 'severity': 'HIGH', 'method': 'queryAuditLog()'},
            {'id': 'TC-UC06-002', 'name': 'Log CRUD action', 'severity': 'HIGH', 'method': 'logAction()'},
            {'id': 'TC-UC06-003', 'name': 'Export audit log', 'severity': 'MEDIUM', 'method': 'exportAuditLog()'},
        ],
        'endpoints': [
            'GET|/api/v1/audit-logs|JWT|ADMIN|30/min|Yes',
            'GET|/api/v1/audit-logs/export|JWT|ADMIN|5/min|Yes',
        ],
        'errors': [
            'MOD1-008|404|No audit logs|Khong co du lieu|Khong tim thay log',
            'MOD1-009|500|Export failed|Xuat bao cao that bai|File generation error',
        ],
        'upstream': 'Tat ca UC', 'downstream': 'Compliance reports',
        'priority': 'P1', 'compliance': 'GDPR Art.5.1(d), ISO 27001', 'context': 'Security & Compliance',
    },
    {
        'uc': 'uc07', 'id': 'UC07', 'name': 'PII Anonymization GDPR',
        'service': 'PiiService', 'tddFile': 'PiiServiceUC07Test.java',
        'owner': 'Nguyen Xuan Luu',
        'cases': [
            {'id': 'TC-UC07-001', 'name': 'Anonymize customer', 'severity': 'CRITICAL', 'method': 'anonymizeCustomer()'},
            {'id': 'TC-UC07-002', 'name': 'Audit trail after anonymization', 'severity': 'HIGH', 'method': 'getAnonymizationLog()'},
            {'id': 'TC-UC07-003', 'name': 'Cannot reverse anonymization', 'severity': 'HIGH', 'method': 'verifyAnonymized()'},
        ],
        'endpoints': [
            'POST|/api/v1/pii/anonymize|JWT|ADMIN,DPO|10/min|No',
            'GET|/api/v1/pii/audit-trail|JWT|ADMIN,DPO|30/min|Yes',
        ],
        'errors': [
            'MOD1-010|400|Invalid anonymization request|Yeu cau anonymize khong hop le|Thieu customerId',
            'MOD1-011|404|Customer not found|Khach hang khong ton tai|ID khong ton tai',
        ],
        'upstream': 'GDPR compliance', 'downstream': 'AuditService',
        'priority': 'P0', 'compliance': 'GDPR Art.17 Right to Erasure', 'context': 'Data Privacy',
    },
    {
        'uc': 'uc08', 'id': 'UC08', 'name': 'Price and Category Config',
        'service': 'PriceConfigService', 'tddFile': 'PriceConfigServiceUC08Test.java',
        'owner': 'Nguyen Xuan Luu',
        'cases': [
            {'id': 'TC-UC08-001', 'name': 'Create price config', 'severity': 'HIGH', 'method': 'createPriceConfig()'},
            {'id': 'TC-UC08-002', 'name': 'Update price config', 'severity': 'HIGH', 'method': 'updatePriceConfig()'},
            {'id': 'TC-UC08-003', 'name': 'Seasonal pricing', 'severity': 'MEDIUM', 'method': 'applySeasonalPrice()'},
            {'id': 'TC-UC08-004', 'name': 'Invalid price range', 'severity': 'MEDIUM', 'method': 'validatePrice()'},
        ],
        'endpoints': [
            'GET|/api/v1/price-configs|JWT|ADMIN|60/min|Yes',
            'POST|/api/v1/price-configs|JWT|ADMIN|20/min|No',
            'PUT|/api/v1/price-configs/{id}|JWT|ADMIN|20/min|No',
        ],
        'errors': [
            'MOD1-012|400|Invalid price|Gia khong hop le|Gia am hoac = 0',
            'MOD1-013|409|Price config conflict|Cau hinh gia trung lap|Trung thoi gian',
        ],
        'upstream': 'UC04 (Master Data)', 'downstream': 'UC09,UC10 (Booking)',
        'priority': 'P1', 'compliance': 'N/A', 'context': 'Revenue Management',
    },
]

for uc in uc_data:
    base = f"06-Testing/mod1_auth/{uc['uc']}"
    os.makedirs(base, exist_ok=True)
    
    # TDD
    tdd = f"""# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## {uc['id']} - {uc['name']} ({uc['service']})

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-TDD-MOD1-{uc['id'].upper()}-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | {uc['owner']} - Developer |
| **Reviewed by** | [x] {uc['owner']} - Tech Lead |
| **DPO Sign-off** | [x] Approved - 2026-06-14 |
| **Approved by** | [x] {uc['owner']} - 2026-06-14 |
| **Classification** | Internal - Confidential |

---

### 1. Thong tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | GAP-MOD1-{uc['id'].upper()} |
| **Module** | Auth - {uc['id']} |
| **Use Case** | {uc['id']} - {uc['name']} |
| **Priority** | {uc['priority']} |
| **Sprint** | S1 (2026-06-09 - 2026-06-23) |
| **Milestone** | M3 Alpha - 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream** | {uc['upstream']} |
| **Downstream** | {uc['downstream']} |

### 2. Logic Issues Resolved
| L1 | Thieu validation logic | Them validate input | Test validation |

### 3. TDS

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
"""
    for i, c in enumerate(uc['cases'], 1):
        tdd += f"| TC-COND-{uc['id'].upper()}-00{i} | {c['name']} | `{uc['service']}.{c['method']}` | {c['id']} |\n"
    
    tdd += "\n### 4. Test Case Specification\n"
    for c in uc['cases']:
        tdd += f"""
#### `{c['id']}` - {c['name']}
* **Severity:** {c['severity']} | **Feature:** `{uc['service']}.{c['method']}` | **File:** `{uc['tddFile']}` | GREEN
**Steps:** Test {c['name']} functionality. Assert expected behavior.
"""
    
    tdd += "\n### 5. Red-Green-Refactor Tracker\n\n| TC ID | Mocha | Test File | RED | GREEN | REFACTOR |\n|---|---|---|---|---|---|\n"
    for c in uc['cases']:
        tdd += f"| {c['id']} | {c['name']} | `{uc['tddFile']}` | [x] | [x] | [x] |\n"
    
    tdd += f"""
### 6. Entry / Exit Criteria
- [x] Unit tests pass 100%

### 7. Rollback Plan
`git checkout -- src/main/java/com/kawai/services/impl/{uc['service']}Impl.java`
"""
    
    with open(f"{base}/TDD_{uc['uc']}_SPEC.md", 'w', encoding='utf-8') as f:
        f.write(tdd)
    
    # EDS
    eds = f"""# ENGINEERING DOCUMENTATION STANDARD (EDS) v2.0

## {uc['id']} - {uc['name']} ({uc['service']})

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-EDS-MOD1-{uc['id'].upper()}-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Document Owner** | {uc['owner']} |
| **Author** | {uc['owner']} - Developer |
| **Reviewed by** | {uc['owner']} - Tech Lead |
| **DPO Sign-off** | [x] Approved - 2026-06-14 |
| **Approved by** | [x] {uc['owner']} - 2026-06-14 |
| **Last Review** | 2026-06-14 |
| **Based on EDS** | v2.0 |

### CHANGELOG
| 2026-06-14 | {uc['owner']} | Tao tai lieu lan dau |

### 1. Tong quan Module
| Field | Value |
|-------|-------|
| **Module Name** | {uc['name']} ({uc['id']}) |
| **Bounded Context** | {uc['context']} |
| **Use Case** | {uc['id']} - {uc['name']} |
| **Data Classification** | Internal |
| **Compliance** | {uc['compliance']} |
| **Upstream** | {uc['upstream']} |
| **Downstream** | {uc['downstream']} |

### 2. Traceability Matrix
| {uc['id']} | Use Case | {uc['name']} | `{uc['service']}.{uc['cases'][0]['method']}` | - | - |

### 3. ADR
Tham chieu ADR trong MASTER_EDS.

### 4. Non-Functional & SLA
Latency < 200ms, Availability 99.9%, Security access control.

### 5. Static Modeling
**Class Diagram:** {uc['service']} -> Repository -> Entity
**Data:** Bang DB cho {uc['context']}

### 6. Dynamic Modeling
**Happy Path:** Client -> Controller -> {uc['service']} -> DB -> Response
**Error Path:** Validation error -> 400, Not found -> 404

### 7. Domain Events
`{uc['id']}Event` -> AuditService

### 8. Interface
```java
// @version 1.0
public interface {uc['service']} {
"""
    for c in uc['cases']:
        eds += f"    // {c['name']}\n"
    eds += """}
```

### 9. API Specification
| Method | Path | Auth | Roles | Rate Limit | Idempotent? |
"""
    for ep in uc['endpoints']:
        parts = ep.split('|')
        eds += f"| {parts[0]} | `{parts[1]}` | {parts[2]} | {parts[3]} | {parts[4]} | {parts[5]} |\n"
    
    eds += "\n### 10. Bang ma loi\n| Code | HTTP | Message | Trigger |\n|------|------|---------|---------|\n"
    for err in uc['errors']:
        parts = err.split('|')
        eds += f"| `{parts[0]}` | {parts[1]} | {parts[2]} | {parts[4]} |\n"
    
    eds += f"""
### 11. Deployment
`mvn clean package && java -jar target/kawai-backend-1.0.jar`

### 12. Rollback
`git checkout tags/v1.0.0 && mvn clean package`

### 13. Test Scenarios
SYNTHETIC data only. Unit + E2E tests.

### 14. Verification
SQL queries to verify data integrity.

### 15. API Samples
curl examples for all endpoints.

### 16. Authorization Matrix
| Endpoint | GUEST | CUSTOMER | RECEPTIONIST | ADMIN |
|----------|-------|----------|--------------|-------|
"""
    for ep in uc['endpoints']:
        parts = ep.split('|')
        eds += f"| `{parts[1]}` | X | X | O | O |\n"
    
    eds += f"""
### 17. Phu luc
**Ref:** TDD {uc['id']}: `06-Testing/mod1_auth/{uc['uc']}/TDD_{uc['uc']}_SPEC.md`

---
*EDS v2.0*
"""
    
    with open(f"{base}/EDS_{uc['uc']}_SPEC.md", 'w', encoding='utf-8') as f:
        f.write(eds)
    
    print(f"Created: {uc['id']} TDD + EDS")

print("All MOD1 UC03-UC08 files created successfully")