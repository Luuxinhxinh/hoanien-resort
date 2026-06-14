# 📂 Quy Tắc Phân Loại Thư Mục Dự Án

> Cấu trúc dựa trên **CMMI-DEV Process Areas** + **IEEE 12207 Software Lifecycle** + Quy trình SWP391.

---

## Tổng Quan Cấu Trúc

```
su26-swp391-se2023-g2/
├── 00-Policy/           ← Chính sách & Quy định nhóm
├── 01-Planning/         ← Kế hoạch dự án
├── 02-Requirement/      ← Yêu cầu & Phân tích
├── 03-Design/           ← Thiết kế hệ thống
├── 04-Implement/        ← Mã nguồn (code chạy được)
├── 05-Development/      ← Hạ tầng phát triển
├── 06-Testing/          ← Kiểm thử
├── 07-Reports/          ← Báo cáo & Biên bản
├── 08-Document-References/  ← Tài liệu tham khảo
├── .agents/             ← (Tooling — không đổi)
├── .gitignore
├── README.md
└── skills-lock.json
```

---

## Chi Tiết Từng Thư Mục

### `00-Policy` — Chính sách & Quy định nhóm

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **CMMI: OPD** (Organizational Process Definition) | Quy trình, quy định, chuẩn mực mà **cả team phải tuân theo** |

**Ví dụ file:**
- `TEAM_WORKFLOW_GUIDE.md` — Quy trình làm việc nhóm
- `CODING_CONVENTION.md` — Chuẩn code (đặt tên biến, format)
- `GIT_BRANCHING_STRATEGY.md` — Chiến lược nhánh Git
- `SECURITY_POLICY.md` — Chính sách bảo mật

> [!TIP]
> **Quy tắc**: Nếu file trả lời câu hỏi *"Nhóm mình làm theo quy tắc gì?"* → vào `00-Policy`

---

### `01-Planning` — Kế hoạch dự án

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **CMMI: PP** (Project Planning) | Lịch trình, phân công, milestone, WBS |

**Ví dụ file:**
- `PROJECT_PLAN.md` — Kế hoạch tổng thể
- `SPRINT_BACKLOG.md` — Danh sách công việc sprint
- `WBS.md` — Work Breakdown Structure
- `GANTT_CHART.xlsx` — Biểu đồ Gantt
- `RESOURCE_ALLOCATION.md` — Phân công nhân sự

> [!TIP]
> **Quy tắc**: Nếu file trả lời *"Ai làm gì, khi nào xong?"* → vào `01-Planning`

---

### `02-Requirement` — Yêu cầu & Phân tích

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **IEEE 830 / CMMI: REQM** (Requirements Management) | SRS, Use Case, Business Rules, Traceability |

**Ví dụ file:**
- `SRS_Document.pdf` — Software Requirement Specification
- `UC_MASTER_TABLE.md` — Bảng Use Case tổng hợp
- `UC_DETAIL_SPEC.md` — Đặc tả Use Case chi tiết
- `BR_ACTOR_ROLE_TABLE.md` — Bảng Business Rules & Actor
- `TC_MASTER_TABLE.md` — Bảng Test Case gốc (gắn với requirement)
- `TRACEABILITY_MATRIX.md` — Ma trận truy vết yêu cầu
- `Project_Specification.md` — Đặc tả dự án

> [!TIP]
> **Quy tắc**: Nếu file trả lời *"Hệ thống phải làm được gì?"* → vào `02-Requirement`

---

### `03-Design` — Thiết kế hệ thống

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **IEEE 1016 / CMMI: TS** (Technical Solution) | Kiến trúc, DB schema, UI mockup, class diagram |

**Ví dụ file:**
- `database_schema.md` — Thiết kế cơ sở dữ liệu
- `FE_Templates/` — Mockup / Prototype giao diện
- `ARCHITECTURE_DIAGRAM.md` — Sơ đồ kiến trúc hệ thống
- `API_DESIGN.md` — Thiết kế API endpoints
- `CLASS_DIAGRAM.puml` — Biểu đồ lớp
- `SEQUENCE_DIAGRAM.puml` — Biểu đồ tuần tự
- `skills/SKILL.md` — Tài liệu kỹ năng thiết kế

> [!TIP]
> **Quy tắc**: Nếu file trả lời *"Hệ thống được xây dựng như thế nào?"* → vào `03-Design`

---

### `04-Implement` — Mã nguồn

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **CMMI: PI** (Product Integration) | Source code, config, build scripts — **tất cả code chạy được** |

**Cấu trúc bên trong:**
```
04-Implement/
├── kawai-backend/        ← Spring Boot project (Java)
│   ├── src/
│   ├── pom.xml
│   └── apache-maven-3.9.6/
├── kawai-ai-service/     ← Python AI service
│   └── main.py
└── .idea/                ← IDE config
```

> [!IMPORTANT]
> **Quy tắc nghiêm ngặt**: CHỈ chứa code có thể compile/run được. KHÔNG để tài liệu `.md` phân tích vào đây.

---

### `05-Development` — Hạ tầng phát triển

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **DevOps / CMMI: CM** (Configuration Management) | CI/CD, Docker, deploy scripts, environment config |

**Ví dụ file:**
- `Dockerfile` — Container config
- `docker-compose.yml` — Multi-container setup
- `.github/workflows/` — CI/CD pipelines
- `DEPLOY_GUIDE.md` — Hướng dẫn deploy
- `ENV_SETUP.md` — Hướng dẫn cài đặt môi trường dev

> [!TIP]
> **Quy tắc**: Nếu file trả lời *"Làm sao để build/deploy/chạy dự án?"* → vào `05-Development`

---

### `06-Testing` — Kiểm thử

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **IEEE 829 / CMMI: VER, VAL** (Verification & Validation) | Test plan, test case docs, EDS, TDD specs |

**Cấu trúc bên trong:**
```
06-Testing/
├── MASTER_EDS_SPEC.md         ← EDS tổng hợp
├── MASTER_TDD_SPEC.md         ← TDD tổng hợp
├── mod1_auth/                 ← Module 1: Authentication
│   ├── EDS_MOD1_SPEC.md
│   └── TDD_MOD1_SPEC.md
├── mod2_booking/              ← Module 2: Booking
├── mod3_pos/                  ← Module 3: POS
├── mod4_tour/                 ← Module 4: Tour
├── mod5_finance/              ← Module 5: Finance
└── templates/                 ← Template mẫu
    ├── EDS_TEMPLATE_V2.0.md
    └── TDD_TEMPLATE_V1.md
```

> [!WARNING]
> **Phân biệt**: Unit test code (`.java`) nằm trong `04-Implement/kawai-backend/src/test/`. Tài liệu kiểm thử (`.md`) nằm ở `06-Testing/`.

---

### `07-Reports` — Báo cáo & Biên bản

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **CMMI: PMC** (Project Monitoring & Control) | Meeting minutes, progress reports, sprint reviews |

**Ví dụ file:**
- `MEETING_NOTES_20260614.md` — Biên bản họp
- `SPRINT_REVIEW_S3.md` — Báo cáo sprint review
- `PROGRESS_REPORT_W5.md` — Báo cáo tiến độ tuần
- `FINAL_PRESENTATION.pptx` — Slide báo cáo cuối kỳ
- `DEMO_CHECKLIST.md` — Checklist demo

> [!TIP]
> **Quy tắc**: Nếu file trả lời *"Dự án đang ở đâu, kết quả thế nào?"* → vào `07-Reports`

---

### `08-Document-References` — Tài liệu tham khảo

| Tiêu chuẩn | File thuộc loại nào vào đây? |
|---|---|
| **Supplementary** | Tài liệu bên ngoài, ghi chú, hướng dẫn tham khảo |

**Ví dụ file:**
- `AGENTS_MEMORIES.md` — Ghi chú AI agent
- `TECH_STACK_REFERENCE.md` — Tham khảo công nghệ
- `SPRING_SECURITY_GUIDE.md` — Hướng dẫn tham khảo
- `THYMELEAF_CHEATSHEET.md` — Bảng tóm tắt Thymeleaf

> [!TIP]
> **Quy tắc**: Nếu file **không thuộc output chính** của dự án nhưng hỗ trợ team → vào `08-Document-References`

---

## Quy Tắc Chung

### ❌ KHÔNG được để vào repo
| Loại file | Lý do |
|---|---|
| `BOOT-INF/`, `target/` | Build artifact — tự sinh khi build |
| `cookies.txt`, `*.env` | Dữ liệu nhạy cảm |
| `node_modules/` | Dependencies — tải bằng `npm install` |
| `.idea/workspace.xml` | Cá nhân từng dev |

### ✅ File ở gốc repo (root)
| File | Lý do |
|---|---|
| `README.md` | Tiêu chuẩn mọi repo |
| `.gitignore` | Cấu hình Git |
| `skills-lock.json` | Tooling config |

---

## Checklist Phân Loại Nhanh

```
File mới cần thêm vào repo?
│
├─ Là quy tắc/quy trình nhóm?           → 00-Policy
├─ Là lịch trình/phân công?              → 01-Planning  
├─ Là yêu cầu hệ thống (UC, BR, SRS)?   → 02-Requirement
├─ Là thiết kế (DB, UI, diagram)?        → 03-Design
├─ Là source code chạy được?             → 04-Implement
├─ Là script deploy/CI/CD?               → 05-Development
├─ Là tài liệu test (EDS/TDD)?          → 06-Testing
├─ Là báo cáo/biên bản?                  → 07-Reports
└─ Là tài liệu tham khảo khác?          → 08-Document-References
```
