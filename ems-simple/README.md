# Equipment Management System: simple version

Java 21 · Spring Boot 3.5 · Thymeleaf · Tailwind (CDN) · MySQL · plain SQL with `JdbcTemplate`

This version is deliberately small and plain. There is **no Lombok, no JPA/Hibernate, no Flyway and no Spring Security**.
You write ordinary Java classes and ordinary SQL, and you can read every line.

## 1. Set up and run

**Install once**

- **JDK 21** (IntelliJ: File > Project Structure > SDKs > Download JDK > 21).
- **MySQL Community Server 8.x and MySQL Workbench**. The official MySQL Installer for Windows installs both. Write down the `root` password you choose during installation. Make sure the MySQL service is running.

**Create the database (once per computer)**

1. Open MySQL Workbench and connect to your local server as `root`.
2. Open each script with File > Open SQL Script and run it with the lightning-bolt button, **in this order**:
   1. `db/00_setup_user.sql`: creates the database `ems_db` and the user `ems_user` / `ems_pass`
   2. `db/01_schema.sql`: creates the tables
   3. `db/02_seed.sql`: adds the demo school, users and equipment

**Run the application in IntelliJ**

1. **File > Open** the folder that contains `pom.xml`. Wait for Maven to finish downloading.
2. Open `src/main/java/com/ems/EmsApplication.java` and click the green arrow next to `main`.
3. Go to http://localhost:8080

| Email | Password | Role |
|---|---|---|
| teacher@school.test | password123 | TEACHER |
| tech@school.test | password123 | TECHNICIAN |
| admin@school.test | password123 | SCHOOL_ADMIN |

**Changed the tables, or the data is messy?** In Workbench run `db/99_reset_database.sql` (it deletes everything), then `01_schema.sql` and `02_seed.sql` again.

**Prefer to use root instead of `ems_user`?** Change `spring.datasource.username` and `spring.datasource.password` in `application.properties` to your root login. Then you can skip `00_setup_user.sql` except for its first line (`CREATE DATABASE`).

**Common problems**

- *Connection refused / Communications link failure:* the MySQL service is not running (Windows: open Services and start MySQL80), or it uses a different port. The project expects 3306.
- *Access denied for user 'ems_user':* you did not run `00_setup_user.sql` while connected as root, or the password was changed.
- *Unknown database 'ems_db':* run the three scripts in order.
- *Table doesn't exist:* run `01_schema.sql` and `02_seed.sql`.
- *Login says invalid email or password:* the seed script did not run. Check that `users` has three rows.

## 2. File structure

```
ems-simple/
├── pom.xml                     dependencies
├── db/
│   ├── 00_setup_user.sql       creates the database and the ems_user account
│   ├── 01_schema.sql           all CREATE TABLE statements
│   ├── 02_seed.sql             demo school, users, equipment
│   └── 99_reset_database.sql   deletes everything (development only)
└── src/main/
    ├── java/com/ems/
    │   ├── EmsApplication.java     starts the program
    │   ├── controller/             one class per area: Auth, Home, Dashboard, Equipment, Report
    │   ├── dao/                    ALL the SQL: UserDao, EquipmentDao, ReportDao, LookupDao
    │   ├── model/                  plain Java classes: User, Equipment, Report, StatusHistory, Option
    │   ├── config/                 LoginInterceptor + WebConfig (the "are you logged in?" check)
    │   └── util/PasswordUtil.java  password hashing
    └── resources/
        ├── application.properties  database settings
        └── templates/              HTML pages (Thymeleaf)
            ├── fragments/layout.html   shared <head>, top bar, badges
            ├── login.html  dashboard.html
            ├── equipment/ (list, form)
            └── reports/ (my, form, detail)
```

## 3. How it works (three layers)

```
Browser  →  Controller  →  DAO  →  MySQL
              ↓
          HTML template  →  Browser
```

| Part | Job | Example |
|---|---|---|
| **Controller** | Receives the request, checks the input, calls the DAO, picks the HTML page | `ReportController.save()` |
| **DAO** (Data Access Object) | Runs SQL and turns rows into Java objects | `ReportDao.create()` |
| **Model** | Plain class that holds data | `Report` |
| **Template** | HTML with small `th:` placeholders | `reports/form.html` |

**Follow one request: a teacher submits a report**

1. The browser sends the form to `POST /reports/save`.
2. `LoginInterceptor` checks that someone is logged in. (It runs before every controller.)
3. `ReportController.save()` checks the input (equipment chosen? description at least 10 characters?).
4. It asks `ReportDao.existsRecent()` whether the equipment was reported in the last 7 days. If so, the form is shown again with a warning.
5. `ReportDao.create()` runs three SQL statements in one `@Transactional` block: insert the report, insert the first history row, set the equipment to DAMAGED.
6. The controller redirects to `/reports/mine`, which shows the list with a success message.

**Spring words you will meet**

- `@Controller` / `@Repository`: Spring creates one object of the class for you. You never write `new ReportDao()`. You ask for it in the constructor (look at any controller).
- `@GetMapping("/x")` shows a page. `@PostMapping("/x")` receives a submitted form.
- `@RequestParam String email`: takes the form field called `email`.
- `@SessionAttribute("user") User user`: gives you the logged-in user.
- `Model`: a box where the controller puts data for the template (`model.addAttribute("reports", list)`).
- `return "reports/my"`: show `templates/reports/my.html`. `return "redirect:/x"`: send the browser to another URL.
- `@Transactional`: either all the SQL in the method works, or none of it is saved.

**Thymeleaf cheat sheet**

| Write | Meaning |
|---|---|
| `th:text="${r.referenceNo}"` | show a value |
| `th:each="r : ${reports}"` | repeat for each item in a list |
| `th:if="${error}"` | show only when `error` is not empty |
| `th:href="@{/reports/{id}(id=${r.id})}"` | build a link |
| `th:value`, `th:selected` | fill a form field or select an option |
| `session.user.staff` | the logged-in user's `isStaff()` |

## 4. Rules for the team

1. **Every query on school data must include the school.** Look at `findBySchool` and `findById`: they have `WHERE school_id = ?`. Never load a record by its id alone. This keeps schools' data apart.
2. **Always use `?` placeholders in SQL**, never join user input into the SQL text. This stops SQL injection.
3. **All SQL lives in the `dao` package.** Controllers never contain SQL.
4. **Changing the database:** edit `db/01_schema.sql`, tell the team, and everyone runs `99_reset_database.sql`, `01_schema.sql` and `02_seed.sql` in Workbench. (Your demo data is lost each time, which is fine in development.)
5. Do not commit `target/` or `.idea/` (already in `.gitignore`).

## 5. How to add a feature (example: borrowing, RQ13)

1. **SQL:** the `borrowings` table already exists in `01_schema.sql`.
2. **Model:** copy `Report.java` to `Borrowing.java`, change the fields, generate getters/setters (IntelliJ: Alt+Insert).
3. **DAO:** copy `EquipmentDao.java` to `BorrowingDao.java`. Write your `SELECT` and `INSERT` statements.
4. **Controller:** copy `EquipmentController.java` to `BorrowingController.java`. Show a form, save it, redirect.
5. **Templates:** copy `equipment/form.html` and `equipment/list.html` into a `borrowing/` folder and edit them.
6. **Menu and security:** add a link in `fragments/layout.html`. For staff-only pages add the URL to `staffOnly` in `LoginInterceptor`.

## 6. What is done and what is left

| Requirement | State |
|---|---|
| RQ01 Submit report | Done: validation, reference number, 7-day duplicate warning |
| RQ02 Photos/videos | TODO: file upload (`report_attachments` table exists) |
| RQ03 Location + GPS | Partly: location dropdown and details. TODO: GPS (JavaScript) and map |
| RQ04 Track reports | Done: my reports, details, status history. TODO: sorting, notifications |
| RQ05 Admin dashboard | Partly: table and status filter. TODO: charts, more filters |
| RQ06 Update status | Done, no email yet |
| RQ07 Notifications | TODO: `notifications` table, email |
| RQ08 Summary reports | TODO: SQL with `GROUP BY`, PDF/CSV |
| RQ09 Search | Partly: equipment search. TODO: search reports |
| RQ10 CSV export | TODO |
| RQ11 Unresolved + urgent | Done (counts, red highlight). TODO: breakdown by category |
| RQ12 Equipment + QR | Partly: register and list. TODO: QR code images and scanning |
| RQ13 Borrowing | TODO (`borrowings` table exists) |
| RQ14 Users, roles, schools | TODO: manage users and schools |
| RQ15 Location, transfer, condition | TODO: edit equipment, `equipment_transfers` table exists |

## 7. Simplifications to mention in your report (limitations)

- Passwords are hashed with plain SHA-256. A real system uses BCrypt with a salt.
- Login uses a simple session check, not a security framework. There is no CSRF protection.
- Validation is basic (HTML `required` plus checks in the controller).
- Tailwind comes from a CDN, so the pages need internet access while developing.
- The `users` table has a `role` column instead of a separate roles table, and there is no departments table. Update your ERD to match.
