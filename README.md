# Finance Dashboard Backend

A RESTful backend API for a Finance Dashboard system, built with **Java 17**, **Spring Boot 3**, **Spring JDBC (JdbcTemplate)**, and **MySQL**.

---

## Table of Contents
1. [Tech Stack](#tech-stack)
2. [Architecture Overview](#architecture-overview)
3. [Project Structure](#project-structure)
4. [Setup & Running the Project](#setup--running-the-project)
5. [API Reference](#api-reference)
6. [Access Control (Roles)](#access-control-roles)
7. [How Authentication Works](#how-authentication-works)
8. [Database Schema](#database-schema)
9. [Design Decisions & Assumptions](#design-decisions--assumptions)
10. [Sample Requests (Quick Start)](#sample-requests-quick-start)

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 17 | LTS version, modern features |
| Framework | Spring Boot 3.2 | Easy setup, embedded Tomcat, auto-config |
| Database Layer | Spring JDBC (JdbcTemplate) | Direct SQL control without ORM magic |
| Database | MySQL 8 | Widely used relational DB for financial data |
| Password Hashing | BCrypt (Spring Security Crypto) | Industry standard for safe password storage |
| Validation | Spring Validation (Jakarta) | Declarative input validation via annotations |
| Build Tool | Maven | Dependency management and packaging |

---

## Architecture Overview

The project follows a classic **3-Layer Architecture**:

```
HTTP Request
     │
     ▼
┌─────────────────────────────────┐
│         AuthFilter              │  ← Runs BEFORE every request
│  (Validates token, sets user)   │    Returns 401 if no/invalid token
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│       Controller Layer          │  ← Receives HTTP request
│  (AuthController, Transaction   │    Calls service, returns JSON
│   Controller, DashboardCtrl..)  │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│        Service Layer            │  ← Business logic lives here
│  (AuthService, TransactionSvc,  │    Role-based access control enforced here
│   DashboardService, UserSvc..)  │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│       Repository Layer          │  ← All SQL queries live here
│  (UserRepository,               │    Uses JdbcTemplate to talk to MySQL
│   TransactionRepository)        │
└────────────────┬────────────────┘
                 │
                 ▼
         MySQL Database
```

---

## Project Structure

```
finance-dashboard/
├── pom.xml                                    # Maven dependencies
└── src/
    └── main/
        ├── java/com/zorvyn/finance/
        │   ├── FinanceDashboardApplication.java  # Entry point (main method)
        │   ├── config/
        │   │   ├── AppConfig.java               # Registers AuthFilter, disables default Spring Security
        │   │   └── DataInitializer.java          # Creates default admin on first startup
        │   ├── controller/
        │   │   ├── AuthController.java           # /api/auth/* endpoints
        │   │   ├── UserController.java           # /api/users/* endpoints
        │   │   ├── TransactionController.java    # /api/transactions/* endpoints
        │   │   └── DashboardController.java      # /api/dashboard/* endpoints
        │   ├── dto/
        │   │   ├── ApiResponse.java              # Generic JSON response wrapper
        │   │   ├── LoginRequest.java             # Login request body shape
        │   │   ├── RegisterRequest.java          # Register request body shape
        │   │   ├── CreateTransactionRequest.java # Create transaction body shape
        │   │   ├── UpdateTransactionRequest.java # Update transaction body shape
        │   │   └── UpdateUserRoleRequest.java    # Update role body shape
        │   ├── exception/
        │   │   ├── AccessDeniedException.java    # Custom 403 exception
        │   │   ├── ResourceNotFoundException.java# Custom 404 exception
        │   │   └── GlobalExceptionHandler.java   # Catches ALL exceptions → clean JSON errors
        │   ├── filter/
        │   │   └── AuthFilter.java               # Token validation filter
        │   ├── model/
        │   │   ├── User.java                     # User database model
        │   │   └── Transaction.java              # Transaction database model
        │   ├── repository/
        │   │   ├── UserRepository.java           # All user SQL queries
        │   │   └── TransactionRepository.java    # All transaction SQL queries
        │   └── service/
        │       ├── AuthService.java              # Register/login/logout logic
        │       ├── UserService.java              # User management logic
        │       ├── TransactionService.java       # Transaction CRUD + access control
        │       └── DashboardService.java         # Analytics + summaries
        └── resources/
            ├── application.properties            # DB config, server port
            └── schema.sql                        # MySQL table definitions (auto-runs on startup)
```

---

## Setup & Running the Project

### Prerequisites
- Java 17+ installed → verify: `java -version`
- Maven 3.6+ installed → verify: `mvn -version`
- MySQL 8 running locally

### Step 1 — Create the Database

Open MySQL and run:
```sql
CREATE DATABASE finance_db;
```

You do NOT need to create any tables — the app does that automatically using `schema.sql`.

### Step 2 — Configure Database Credentials

Open `src/main/resources/application.properties` and update:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root         # ← your MySQL username
spring.datasource.password=your_password # ← your MySQL password
```

### Step 3 — Run the Application

```bash
# Option A: Using Maven
mvn spring-boot:run

# Option B: Build a JAR and run it
mvn clean package -DskipTests
java -jar target/finance-dashboard-0.0.1-SNAPSHOT.jar
```

### Step 4 — Verify It's Running

You should see this in the terminal:
```
========================================================
  Finance Dashboard API is running!
  Base URL : http://localhost:8080/api
  Default Admin : admin@finance.com / admin123
========================================================
```

The app automatically creates a default admin account on first start:
- **Email**: `admin@finance.com`
- **Password**: `admin123`

---

## API Reference

All responses follow this format:
```json
{
  "success": true,
  "message": "Some message",
  "data": { ... }
}
```

---

### Authentication Endpoints

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```
New users are always created as **VIEWER**. Only an Admin can promote them.

---

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@finance.com",
  "password": "admin123"
}
```
**Response includes a token:**
```json
{
  "success": true,
  "data": {
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "role": "ADMIN",
    "email": "admin@finance.com"
  }
}
```
Save this token — include it in all subsequent requests as:
```
Authorization: Bearer 550e8400-e29b-41d4-a716-446655440000
```

---

#### Logout
```
POST /api/auth/logout
Authorization: Bearer <token>
```

---

#### Get Current User Profile
```
GET /api/auth/me
Authorization: Bearer <token>
```

---

### Transaction Endpoints

All transaction endpoints require: `Authorization: Bearer <token>`

#### Create Transaction *(ANALYST, ADMIN only)*
```
POST /api/transactions
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 50000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-03-01",
  "notes": "March salary"
}
```

#### Get All Transactions *(All roles)*
```
GET /api/transactions
GET /api/transactions?type=INCOME
GET /api/transactions?category=Rent
GET /api/transactions?from=2024-01-01&to=2024-03-31
GET /api/transactions?type=EXPENSE&category=Food
```

#### Get One Transaction *(All roles)*
```
GET /api/transactions/{id}
```

#### Update Transaction *(ADMIN only)*
```
PUT /api/transactions/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 52000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-03-01",
  "notes": "March salary (updated)"
}
```

#### Delete Transaction *(ADMIN only)*
```
DELETE /api/transactions/{id}
Authorization: Bearer <token>
```
This is a **soft delete** — the record is marked as deleted but stays in the database.

---

### Dashboard Endpoints

All dashboard endpoints require: `Authorization: Bearer <token>`

#### Summary *(All roles)*
```
GET /api/dashboard/summary
```
Returns total income, total expenses, net balance, and surplus/deficit status.

#### Category Breakdown *(ANALYST, ADMIN only)*
```
GET /api/dashboard/categories
```

#### Monthly Trends *(ANALYST, ADMIN only)*
```
GET /api/dashboard/trends/monthly
```

#### Weekly Trends *(ANALYST, ADMIN only)*
```
GET /api/dashboard/trends/weekly
```

#### Recent Activity *(ANALYST, ADMIN only)*
```
GET /api/dashboard/recent
GET /api/dashboard/recent?limit=5
```

---

### User Management Endpoints

All user management endpoints require ADMIN role.

#### List All Users
```
GET /api/users
Authorization: Bearer <admin-token>
```

#### Get User by ID
```
GET /api/users/{id}
Authorization: Bearer <admin-token>
```

#### Change User Role
```
PATCH /api/users/{id}/role
Authorization: Bearer <admin-token>
Content-Type: application/json

{ "role": "ANALYST" }
```
Valid roles: `VIEWER`, `ANALYST`, `ADMIN`

#### Change User Status
```
PATCH /api/users/{id}/status
Authorization: Bearer <admin-token>
Content-Type: application/json

{ "status": "INACTIVE" }
```
Valid statuses: `ACTIVE`, `INACTIVE`

---

## Access Control (Roles)

| Action | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Login / Logout | ✅ | ✅ | ✅ |
| View own profile | ✅ | ✅ | ✅ |
| View all transactions | ✅ | ✅ | ✅ |
| View dashboard summary | ✅ | ✅ | ✅ |
| Create transactions | ❌ | ✅ | ✅ |
| View category breakdown | ❌ | ✅ | ✅ |
| View trends & recent activity | ❌ | ✅ | ✅ |
| Update transactions | ❌ | ❌ | ✅ |
| Delete transactions | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

---

## How Authentication Works

1. **Register or use the default admin** to get started.
2. **Call `/api/auth/login`** with your email + password → you receive a UUID token.
3. **Include the token** in every subsequent API call via the `Authorization` header:
   ```
   Authorization: Bearer your-uuid-token-here
   ```
4. **`AuthFilter`** intercepts every `/api/*` request:
   - Reads the `Authorization` header
   - Looks up the token in the `auth_tokens` table
   - If valid: fetches the user and attaches them to the request
   - If invalid/missing: immediately returns `401 Unauthorized`
5. **Logout** deletes the token from the database, instantly invalidating it.

> Passwords are hashed with **BCrypt** before storage. Plain-text passwords are never stored.

---

## Database Schema

### `users` table
| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | Auto increment |
| name | VARCHAR(100) | User's display name |
| email | VARCHAR(150) | Unique, used for login |
| password | VARCHAR(255) | BCrypt hashed |
| role | ENUM | VIEWER / ANALYST / ADMIN |
| status | ENUM | ACTIVE / INACTIVE |
| created_at | TIMESTAMP | Auto set on insert |

### `auth_tokens` table
| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | Auto increment |
| user_id | BIGINT (FK) | References users.id |
| token | VARCHAR(255) | UUID, unique |
| created_at | TIMESTAMP | When the session started |

### `transactions` table
| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | Auto increment |
| user_id | BIGINT (FK) | Who created this record |
| amount | DECIMAL(15,2) | Precise money value |
| type | ENUM | INCOME / EXPENSE |
| category | VARCHAR(100) | e.g. "Salary", "Rent" |
| date | DATE | Date of transaction |
| notes | TEXT | Optional description |
| is_deleted | BOOLEAN | Soft delete flag |
| created_at | TIMESTAMP | Auto set on insert |
| updated_at | TIMESTAMP | Auto updated on change |

---

## Design Decisions & Assumptions

1. **JdbcTemplate over JPA/Hibernate**: The assignment asked for JDBC. JdbcTemplate gives us direct SQL control while still removing boilerplate (no manual connection management, prepared statements are safe from SQL injection).

2. **Token-based auth over Spring Security sessions**: We use UUIDs stored in the database as tokens. This is simple, stateless-friendly, and easy to reason about for an assessment context.

3. **Soft deletes for transactions**: Financial records should never be physically removed. Soft delete (setting `is_deleted = true`) preserves data integrity and allows recovery.

4. **Access control in the Service layer**: Role checks are in service classes, not just controllers. This ensures the rules apply consistently regardless of how the service is invoked.

5. **`BigDecimal` for money**: `double`/`float` have floating-point precision issues. `BigDecimal` is the correct type for financial amounts.

6. **New users default to VIEWER role**: Least-privilege principle. A user must be explicitly promoted by an Admin.

7. **One active token per user**: When a user logs in, any previous token is deleted first. This prevents multiple concurrent sessions.

8. **Full updates only (PUT, not PATCH) for transactions**: For simplicity, updating a transaction requires providing all fields. Partial updates (PATCH) are not implemented in this version.

---

## Sample Requests (Quick Start)

Here is a full flow using `curl`:

```bash
# 1. Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@finance.com","password":"admin123"}'

# → Copy the token from the response

# 2. Create a transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"amount":50000,"type":"INCOME","category":"Salary","date":"2024-03-01","notes":"March salary"}'

# 3. Get dashboard summary
curl http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 4. Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Viewer","email":"jane@example.com","password":"pass123"}'

# 5. Promote them to ANALYST (replace 2 with actual user id)
curl -X PATCH http://localhost:8080/api/users/2/role \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"role":"ANALYST"}'
```

> You can also use **Postman** — import the base URL `http://localhost:8080` and test each endpoint using the reference above.
