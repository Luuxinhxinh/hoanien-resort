# ENGINEERING DOCUMENTATION STANDARD (EDS) v2.0

## UC08 - Price and Category Config (PriceConfigService)

| Field | Value |
|-------|-------|
| **Document ID** | KAWAI-EDS-MOD1-UC08-001 |
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
| **Module Name** | Price and Category Config (UC08) |
| **Bounded Context** | Revenue Management |
| **Use Case** | UC08 - Price and Category Config |
| **Data Classification** | Internal |
| **Compliance** | N/A |
| **Upstream** | UC04 (Master Data) |
| **Downstream** | UC09,UC10 (Booking) |

### 2. Traceability Matrix
| UC08 | Use Case | Price and Category Config | `PriceConfigService.createPriceConfig()` | - | - |

### 3. ADR
Tham chieu ADR trong MASTER_EDS.

### 4. Non-Functional & SLA
Latency < 200ms, Availability 99.9%, Security access control.

### 5. Static Modeling
**Class Diagram:** PriceConfigService -> Repository -> Entity
**Data:** Bang DB cho Revenue Management

### 6. Dynamic Modeling
**Happy Path:** Client -> Controller -> PriceConfigService -> DB -> Response
**Error Path:** Validation error -> 400, Not found -> 404

### 7. Domain Events
`UC08Event` -> AuditService

### 8. Interface
```java
// @version 1.0
public interface PriceConfigService 
    for c in uc['cases']:
        eds += f"    // {c['name']}
"
    eds += 
```

### 9. API Specification
| Method | Path | Auth | Roles | Rate Limit | Idempotent? |
| GET | `/api/v1/price-configs` | JWT | ADMIN | 60/min | Yes |
| POST | `/api/v1/price-configs` | JWT | ADMIN | 20/min | No |
| PUT | `/api/v1/price-configs/{id}` | JWT | ADMIN | 20/min | No |

### 10. Bang ma loi
| Code | HTTP | Message | Trigger |
|------|------|---------|---------|
| `MOD1-012` | 400 | Invalid price | Gia am hoac = 0 |
| `MOD1-013` | 409 | Price config conflict | Trung thoi gian |

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
| `/api/v1/price-configs` | X | X | O | O |
| `/api/v1/price-configs` | X | X | O | O |
| `/api/v1/price-configs/{id}` | X | X | O | O |

### 17. Phu luc
**Ref:** TDD UC08: `06-Testing/mod1_auth/uc08/TDD_uc08_SPEC.md`

---
*EDS v2.0*
