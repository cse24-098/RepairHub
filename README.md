# Equipment Management System (EMS)

Multi-school equipment management with maintenance reporting.
**Stack:** Java 21 · Spring Boot 3.5 · Thymeleaf · Tailwind CSS · MySQL 8 · Flyway · Maven

## 1. Run it locally

You need: **JDK 21**, **Maven** (or an IDE with Maven support: IntelliJ IDEA recommended), **Docker** (for MySQL).

```bash
docker compose up -d          # starts MySQL on port 3306 (db: ems_db, user: ems_user)
mvn spring-boot:run           # or run EmsApplication from your IDE
```

Open http://localhost:8080. Demo accounts (created on first start, password `password123`):

| Email | Role | Lands on |
|---|---|---|
| teacher@school.test | TEACHER | My Reports |
| tech@school.test | TECHNICIAN | Dashboard |
| admin@school.test | SCHOOL_ADMIN | Dashboard |

**Lombok:** the code uses Lombok (`@Getter`, `@Setter`). In IntelliJ enable *Settings > Build > Compiler > Annotation Processors > Enable annotation processing*. In Eclipse/VS Code install the Lombok plugin.

**No MySQL via Docker?** Create the database and user yourself, or set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables.

## 2. Project structure

```
ems/
├── pom.xml                         Maven: dependencies and build
├── docker-compose.yml              Local MySQL for everyone
├── README.md
└── src/main/
    ├── java/com/ems/
    │   ├── EmsApplication.java     Starts the app
    │   ├── config/                 SecurityConfig (who may open which URL), DataInitializer (demo data)
    │   ├── security/               AppUserDetails + AppUserDetailsService (login support)
    │   ├── controller/             Receive web requests, call services, choose the HTML template
    │   ├── service/                Business rules (the "brain"); one service per feature area
    │   ├── repository/             Database access (interfaces, no SQL needed for simple queries)
    │   ├── model/                  Entities = Java classes mapped to MySQL tables
    │   │   └── enums/              Fixed value lists (ReportStatus, Priority, ...)
    │   └── dto/                    Form objects: the data typed into a form, plus validation rules
    └── resources/
        ├── application.properties  Settings (database, ports)
        ├── db/migration/           Flyway SQL scripts: V1 schema, V2 seed data, V3... yours
        ├── templates/              Thymeleaf HTML pages
        │   ├── fragments/layout.html   Shared <head>, navbar, badges
        │   ├── equipment/ reports/     One folder per feature
        │   ├── dashboard.html  login.html
        └── static/                 css/ js/ images/ (served as /css/..., /js/...)
```

## 3. How a request flows (read this first)

Example: a teacher submits a report (`POST /reports/save`).

```
Browser form
  → ReportController.save()        1. Spring fills a ReportForm; @Valid checks the rules in the DTO
  → ReportService.submit()         2. Business logic: find equipment, build report, set status, save history
  → MaintenanceReportRepository    3. Saves the MaintenanceReport entity to MySQL
  → redirect to /reports/mine      4. Controller picks the next page
  → ReportController.mine()        5. Puts data in the Model
  → templates/reports/my.html      6. Thymeleaf turns the Model into HTML
```

**The rule of thumb for each layer**

| Layer | Does | Must NOT |
|---|---|---|
| Controller | Read the request, call ONE service, return a template name or redirect | Contain business rules or database code |
| Service | Business rules, permissions on data, transactions (`@Transactional`) | Know about HTML or HTTP |
| Repository | Read/write the database | Contain business rules |
| Entity (model) | Describe one table | Contain logic beyond tiny helpers |
| DTO | Hold form input and validation rules | Be saved directly to the database |
| Template | Display data | Contain business logic |

**Annotations you will see**

- `@Controller`, `@Service`, `@Component`: Spring creates the object for you. Other classes receive it through their **constructor** (dependency injection). You never write `new ReportService()`.
- `@GetMapping("/x")` / `@PostMapping("/x")`: which URL a method handles.
- `@Entity`, `@Table`, `@Column`, `@ManyToOne`: map a class to a table (JPA/Hibernate).
- `@Valid` + `BindingResult`: run the validation rules in the DTO and collect errors.
- `@AuthenticationPrincipal AppUserDetails user`: the logged-in user (id, school, role).
- `@Transactional`: the whole method succeeds or is rolled back.

**Repositories without SQL:** a method named `findByTeacherIdOrderByCreatedAtDesc(Long id)` is turned into a query automatically. For harder queries use `@Query` (see `EquipmentRepository.search`).

## 4. Rules for the whole team

1. **Multi-school safety:** every query for school data must filter by `user.getSchoolId()`. Look at how `findByIdAndSchoolId` is used. Never load a record by id alone.
2. **Database changes go through Flyway only.** Add a new file `V3__short_description.sql`. NEVER edit V1/V2 or any migration that has been pushed. Hibernate is set to `ddl-auto=none`, so it will not change tables. If two people need a migration at the same time, agree on version numbers in the group chat first.
3. **Column names:** Java `fullName` maps to MySQL `full_name` automatically. Enum fields use `@Enumerated(EnumType.STRING)`.
4. **Never delete reports or equipment**; change the status (e.g. RETIRED).
5. **Passwords** are always stored with BCrypt (`PasswordEncoder`). The demo accounts and `DataInitializer` are for development only.
6. **Styling:** Tailwind classes via CDN in `fragments/layout.html`. Before deployment, replace it with the Tailwind CLI build (the standalone CLI needs no Node). Do not mix Tailwind and Bootstrap on the same page.

## 5. Adding a feature (checklist)

Say you are adding "Borrowing" (RQ13):

1. SQL: the `borrowings` table already exists in V1. If you need changes, add `V3__...sql`.
2. `model/Borrowing.java`: copy the style of `MaintenanceReport.java`.
3. `repository/BorrowingRepository.java`: extend `JpaRepository<Borrowing, Long>`.
4. `dto/BorrowForm.java`: fields and `@NotNull` / `@NotBlank` rules.
5. `service/BorrowingService.java`: the rules (equipment must be AVAILABLE, set IN_USE, record condition out, and so on).
6. `controller/BorrowingController.java`: `@GetMapping` to show the form, `@PostMapping` to save.
7. `templates/borrowing/*.html`: copy `equipment/form.html` and `equipment/list.html`.
8. `SecurityConfig`: restrict the URLs if needed. Add a navbar link in `fragments/layout.html`.

## 6. Status of the requirements

| Requirement | State | Where / what is left |
|---|---|---|
| RQ01 Submit report | Mostly done | `ReportController`, `ReportService`, `reports/form.html`. Left: response-time checks |
| RQ02 Photos/videos | TODO | Add `ReportAttachment` entity, upload handling, file storage, thumbnails |
| RQ03 Location + GPS | Partly | Dropdown + free text done. Left: GPS via `navigator.geolocation`, map preview (Leaflet) |
| RQ04 Teacher tracking | Mostly done | `reports/my.html`, `detail.html` with history. Left: sorting, in-app notifications |
| RQ05 Admin dashboard | Partly | Table, filter by status, counts. Left: charts, more filters, pagination |
| RQ06 Update status | Done (no email) | `ReportService.updateStatus`. Left: send email |
| RQ07 Notifications | TODO | `Notification` + `EmailLog` entities, Spring Mail, notification history page |
| RQ08 Summary reports | TODO | Aggregate queries, PDF/CSV export |
| RQ09 Search | Partly | Equipment search done. Left: report search (reference, keywords, date) |
| RQ10 CSV export | TODO | Controller returning `text/csv` |
| RQ11 Unresolved + urgent | Done (basic) | Count and red highlighting. Left: breakdown by category/location, live refresh |
| RQ12 Equipment + QR | Partly | Register + list done. Left: QR image generation (ZXing), QR scanning (html5-qrcode), categories admin |
| RQ13 Borrowing | TODO | Table exists; see section 5 |
| RQ14 Users, roles, schools | TODO | User CRUD, school management, SYSTEM_ADMIN views (that role has no school: handle null `schoolId`) |
| RQ15 Location/transfer/condition | TODO | `equipment_transfers` table exists; edit equipment, move equipment |

**Suggested split for a team:** (A) RQ02 + RQ03, (B) RQ07 + RQ06 email, (C) RQ05 charts + RQ08 + RQ10, (D) RQ12 QR + RQ13 + RQ15, (E) RQ14 users/schools + RQ09. Work on feature branches (`feature/rq07-notifications`), open pull requests into `main`, and review each other's code.

## 7. Git workflow

```bash
git checkout main && git pull
git checkout -b feature/rq13-borrowing
# ... code, test ...
git add . && git commit -m "RQ13: add borrowing entity and service"
git push -u origin feature/rq13-borrowing   # then open a pull request
```
Commit `pom.xml`, `src/` and `docker-compose.yml`; never commit `target/`, `.idea/` or passwords (see `.gitignore`).
