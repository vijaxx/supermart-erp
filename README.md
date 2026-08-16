# Supermart ERP

A small Employee & Inventory Management System built to demonstrate core server-side Java web
development: Servlets + JSP + JDBC in an MVC layout, session-based auth with role separation,
parameterised SQL, salted password hashing, and multi-table reporting.

Built as a portfolio piece by **Kondani Vijay Vardhan** ([github.com/vijaxx](https://github.com/vijaxx)).

## Run it in one command

Requires Java 17+ and Maven. From the project root:

```bash
mvn clean compile exec:java
```

Then open **http://localhost:8080/login**. The database schema and demo data are created
automatically on first run (an H2 file appears under `./data/`) — there is nothing else to set up.

To run the test suite:

```bash
mvn clean test
```

### Demo credentials

These are throwaway accounts seeded for this demo only, **not real credentials**:

| Username | Password    | Role  |
|----------|-------------|-------|
| `admin`  | `Admin@123` | ADMIN |
| `staff`  | `Staff@123` | STAFF |

ADMIN can manage employees, manage inventory, and view reports. STAFF can view and adjust
inventory stock levels but is blocked from employee management and reports.

## Architecture (MVC)

```
com.supermart
├── web/        Servlets (Controller) — read request params, call a service, pick a JSP view.
│                No SQL and no business rules live here.
├── service/    Business rules (validation, low-stock policy, auth policy). Depends on DAOs,
│                not on the servlet API.
├── dao/        JDBC persistence (Model access). One class per table/aggregate root. Every
│                query is a PreparedStatement.
├── model/      Plain POJOs (Employee, Product, User, ...) plus model.report/ for report row DTOs.
├── security/   PasswordHasher, AuthFilter (session guard), AdminFilter (role guard).
└── config/     Database (JDBC connection factory), SchemaInitializer, AppContext (simple DI).
```

JSPs live under `WEB-INF/views/` (not directly reachable — only servlets can forward to them) and
use JSTL (`<c:out>`, `<c:forEach>`, `<fmt:formatNumber>`) instead of scriptlets.

The layers are kept genuinely separate: `EmployeeServlet` never touches SQL; `EmployeeDao` never
validates business rules; `EmployeeService` never knows about `HttpServletRequest`. This is
enforced by what each package imports, not just convention — `dao` and `service` have zero
dependency on `jakarta.servlet.*` (except the filters, which are servlet infrastructure by nature).

## Security

This is the part most relevant to an interview, so it's called out explicitly:

### 1. SQL injection — PreparedStatement everywhere, with a test that proves it

Every single query across every DAO (`UserDao`, `EmployeeDao`, `ProductDao`, `ReportDao`, ...) uses
`PreparedStatement` with bound parameters. There is no string concatenation of user input into SQL
anywhere in the codebase.

`AuthServiceTest` contains the regression test that matters:

```java
@Test
void classicSqlInjectionPayloadInUsernameDoesNotAuthenticate() {
    assertTrue(authService.authenticate("' OR '1'='1", "anything").isEmpty());
}
```

Because `UserDao.findByUsername` binds the username as a parameter
(`WHERE username = ?`), a payload like `' OR '1'='1` is treated as a literal (nonexistent)
username — not as SQL — and the query correctly returns no row. Three more injection variants
(password-field injection, comment truncation `admin' -- `, and a UNION-based attempt) are tested
alongside it. This was also verified live against the running server:

```
$ curl -X POST http://localhost:8080/login \
    --data-urlencode "username=' OR '1'='1" --data-urlencode "password=anything"
HTTP 401 — "Invalid username or password."
```

### 2. Passwords: salted PBKDF2, never plaintext

`PasswordHasher` (in `security/`) hashes passwords with **PBKDF2-HMAC-SHA256**, 120,000
iterations, a fresh random 16-byte salt per password, stored as
`pbkdf2$<iterations>$<salt>$<hash>`. Verification uses `MessageDigest.isEqual` (constant-time
comparison) to avoid leaking timing information. Seed users are created through this same code
path (`SchemaInitializer.seedUsers`) — the demo passwords above are hashed on first run exactly
like a real signup would be; the plaintext values never touch the database.

### 3. Output escaping (XSS)

All dynamic output in JSPs goes through JSTL `<c:out>`, which HTML-escapes by default. There are
no `<%= %>` scriptlet expressions anywhere in a view. User-controlled values that need to sit
inside an HTML attribute (e.g. re-populating a form after a validation error) are still passed
through `<c:out>`.

### 4. Session handling

- `AuthFilter` guards every URL except `/login` and `/logout`; no session with an authenticated
  user means an immediate redirect to the login page.
- `AdminFilter` additionally guards `/employees*` and `/reports`; a authenticated STAFF user gets a
  genuine HTTP 403 (verified with curl below), not just a hidden link.
- On login, any pre-existing session is invalidated before a new one is created (session-fixation
  defence), and the new session gets a fixed timeout.
- `/logout` calls `HttpSession.invalidate()`, so the old session id cannot be replayed — verified
  with curl below.
- The login failure message is the same for "unknown user" and "wrong password", to avoid
  leaking which usernames exist.

## Reporting (JOIN-based SQL)

`ReportDao` is deliberately SQL-first: aggregation happens in the database via `JOIN` +
`GROUP BY`, not by looping over rows in Java. Three reports, all admin-only:

- **Employees per department** — `departments LEFT JOIN employees`, grouped by department, with
  `COUNT`, `SUM`, `AVG`, `MAX` on salary. `LEFT JOIN` so a department with zero employees still
  shows a row.
- **Inventory valuation by category** — `categories LEFT JOIN products`, grouped by category,
  summing `unit_price * stock_quantity`.
- **Low-stock report** — a three-table `products JOIN categories JOIN suppliers`, filtered to
  `stock_quantity <= reorder_level`, so it tells you *what* is low, *what category* it's in, and
  *who to reorder it from* in one query.

`ReportDaoTest` checks these aren't just syntactically valid but numerically correct — e.g. the
sum of each department's `total_salary` must equal the sum of every individual employee's salary.

## What's tested (60 JUnit 5 tests, `mvn clean test`)

| Area | Examples |
|---|---|
| DAO CRUD (`dao/`) | insert/find/update/delete round-trips for employees, products, users, against a real H2 schema |
| SQL injection (`AuthServiceTest`) | tautology, comment-truncation, and UNION payloads all fail to authenticate |
| Password hashing (`PasswordHasherTest`) | correct password verifies, wrong password fails, salts differ per hash, malformed hash fails closed |
| Auth/role enforcement (`AuthFilterTest`, `AdminFilterTest`) | no session → redirect; STAFF on an admin route → 403, request never reaches the servlet; ADMIN → passes through |
| Business validation (`EmployeeServiceTest`, `InventoryServiceTest`) | salary floor, email format, future joining dates, unknown department/category rejected |
| Low-stock logic | boundary case (stock == reorder level counts as low), stock cannot go negative |
| Report aggregation (`ReportDaoTest`) | per-department salary sum equals total payroll; per-category inventory value sums to the grand total |

Every DAO/service test runs against a **fresh in-memory H2 database per test** (`TestDatabases.fresh()`),
schema-initialised and seeded the same way the real app is, so the tests exercise real SQL, not mocks.

## Verified against the running server (curl)

With the server started via `mvn exec:java`, the following was run and observed:

```
GET  /dashboard (no cookie)                          -> 302 to /login?error=session
POST /login  username=admin  password=Admin@123      -> 302 to /dashboard
POST /login  username=' OR '1'='1  password=anything  -> 401, "Invalid username or password."
GET  /reports        (as admin)                      -> 200, department/category/low-stock tables
POST /login  username=staff  password=Staff@123      -> 302 to /dashboard
GET  /reports        (as staff)                      -> 403, "403 - Access denied"
GET  /employees      (as staff)                      -> 403, "403 - Access denied"
GET  /products        (as staff)                     -> 200 (staff is allowed to view/adjust inventory)
GET  /logout          (as admin)                      -> 302; session invalidated
GET  /dashboard (old admin cookie, post-logout)       -> 302 to /login?error=session
```

## Limitations, honestly

- **H2 instead of MySQL.** There is no MySQL available in the environment this was built in, and
  the brief explicitly disallows installing one. The app uses an **embedded H2 database in MySQL
  compatibility mode** (`MODE=MySQL` in the JDBC URL) so the SQL dialect is close to MySQL's, and
  all persistence goes through plain JDBC + `PreparedStatement` — no ORM, no H2-specific query
  syntax in the DAOs. Switching to MySQL in a real deployment means changing `Database.file(...)`
  to a MySQL JDBC URL/driver and nothing in the DAO/service/web layers would need to change. This
  is a deliberate, documented substitution, not an oversight.
- **No connection pooling.** Each DAO call opens and closes its own `Connection`. Fine for a demo
  and for H2; a production MySQL deployment would add HikariCP.
- **No CSRF tokens.** State-changing POSTs (delete employee, adjust stock) rely on the session
  cookie alone. A production version would add a CSRF token per form.
- **Single-node, file-backed H2.** Not designed for concurrent multi-instance deployment.
- **Minimal input validation on the client side.** Validation is enforced server-side in the
  service layer (which is what's tested and what actually matters security-wise), but the HTML
  forms don't do much beyond `required`/`type` attributes.
- **No password reset / account management flows.** Only the two seeded demo accounts exist;
  there's no self-service signup, since that wasn't in scope for this demo.

---

## License

MIT.
