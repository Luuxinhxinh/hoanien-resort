# ENGINEERING DOCUMENTATION STANDARD (EDS) v2.0

## UC07 - PII Anonymization GDPR (PiiService)

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-EDS-MOD1-UC07-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Document Owner** | Nguyen Xuan Luu |
| **Author** | Nguyen Xuan Luu - Developer |
| **Reviewed by** | Nguyen Xuan Luu - Tech Lead |
| **DPO Sign-off** | [x] Approved - 2026-06-14 |
| **Approved by** | [x] Nguyen Xuan Luu - 2026-06-14 |
| **Last Review** | 2026-06-14 |
| **Based on EDS** | v2.0 |

### CHANGELOG
| 2026-06-14 | Nguyen Xuan Luu | Tao tai lieu lan dau |

### 1. Tong quan Module
| Field | Value |
|-------|-------|
| **Module Name** | PII Anonymization GDPR (UC07) |
| **Bounded Context** | Data Privacy |
| **Use Case** | UC07 - PII Anonymization GDPR |
| **Data Classification** | Internal |
| **Compliance** | GDPR Art.17 Right to Erasure |
| **Upstream** | GDPR compliance |
| **Downstream** | AuditService |

### 2. Traceability Matrix
| UC07 | Use Case | PII Anonymization GDPR | `PiiService.anonymizeCustomer()` | - | - |

### 3. ADR
Tham chieu ADR trong MASTER_EDS.

### 4. Non-Functional & SLA
Latency < 200ms, Availability 99.9%, Security access control.

### 5. Static Modeling
**Class Diagram:** PiiService -> Repository -> Entity
**Data:** Bang DB cho Data Privacy

### 6. Dynamic Modeling
**Happy Path:** Client -> Controller -> PiiService -> DB -> Response
**Error Path:** Validation error -> 400, Not found -> 404

### 7. Domain Events
`UC07Event` -> AuditService

### 8. Interface
```java
// @version 1.0
public interface PiiService 
    for c in uc['cases']:
        eds += f"    // {c['name']}
"
    eds += 
```

### 9. API Specification
| Method | Path | Auth | Roles | Rate Limit | Idempotent? |
| POST | `/api/v1/pii/anonymize` | JWT | ADMIN,DPO | 10/min | No |
| GET | `/api/v1/pii/audit-trail` | JWT | ADMIN,DPO | 30/min | Yes |

### 10. Bang ma loi
| Code | HTTP | Message | Trigger |
|------|------|---------|---------|
| `MOD1-010` | 400 | Invalid anonymization request | Thieu customerId |
| `MOD1-011` | 404 | Customer not found | ID khong ton tai |

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
| `/api/v1/pii/anonymize` | X | X | O | O |
| `/api/v1/pii/audit-trail` | X | X | O | O |

### 17. Phu luc
**Ref:** TDD UC07: `06-Testing/mod1_auth/uc07/TDD_uc07_SPEC.md`

---
*EDS v2.0*
