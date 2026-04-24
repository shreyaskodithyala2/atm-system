# ATM Management System

A full-featured, object-oriented ATM simulation built with **Spring Boot 3.2.4** and **Java 17**. Supports three user roles — Customer, Bank Manager, and System Administrator — with a clean MVC architecture, five SOLID principles, and seven design patterns.

---

## Team

| Name | SRN |
|------|-----|
| Sanjit Dev Maheswaran | PES2UG23CS531 |
| Shreyas Kodithyala | PES2UG23CS563 |
| Sigineni Venkata Sanath Reddy | PES2UG23CS579 |
| Souriesh PB | PES2UG23CS588 |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2.4, Java 17 |
| Web MVC | Spring MVC + Thymeleaf |
| Security | Spring Security 6 (dual filter chain) |
| Persistence | Spring Data JPA + H2 (file-based) |
| Build | Maven |
| Server | Embedded Tomcat (port 8080) |

---

## Features

- **Customer authentication** — 16-digit card number + 4-digit PIN with interceptor-based session management
- **Card retention** — card locked automatically after 3 consecutive failed PIN attempts
- **4 transaction types** — Cash Withdrawal, Cash/Cheque Deposit, Fund Transfer, Bill Payment
- **Large-transaction approval workflow** — transactions above $5,000 are flagged `AWAITING_APPROVAL` and routed to the Bank Manager
- **Bank Manager portal** — approve/reject pending transactions, full audit log
- **System Administrator portal** — run hardware diagnostics, update firmware version, retain/release cards
- **Dual Spring Security chains** — form-based login for staff (manager/admin), interceptor-based sessions for customers
- **Hardware simulation** — `ATMHardwareController` Singleton simulates cash dispenser and card reader
- **Real-time diagnostics** — live hardware status, session count, transaction count via Builder-constructed report

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Run

```bash
cd atm-system
mvn spring-boot:run
```

The app starts at **http://localhost:8080**

The H2 database file is created automatically at `./data/atmdb` on first run.

---

## Test Credentials

### Customers (ATM interface → http://localhost:8080)

| Name | Card Number | PIN | Balance | Status |
|------|-------------|-----|---------|--------|
| Alice Johnson | `4111111111111111` | `1234` | $8,000 | Active |
| Bob Smith | `5500005555555559` | `5678` | $12,000 | Active |
| Carol White | `4000000000000002` | `9999` | $150 | Active (low balance) |
| Diana Prince | `4111111111111119` | `2468` | $5,500 | **Retained** |
| Ethan Hunt | `5500005555555567` | `1357` | $3,200 | **Retained** |

### Staff (form login → http://localhost:8080/staff/login)

| Role | Username | Password |
|------|----------|----------|
| Bank Manager | `MGR001` | `manager123` |
| System Admin | `ADM001` | `admin123` |

---

## URL Reference

| Path | Role | Description |
|------|------|-------------|
| `/` | Customer | Card insertion screen |
| `/atm/insert-card` | Customer | Enter card number |
| `/atm/pin-entry` | Customer | Enter PIN |
| `/atm/session/dashboard` | Customer | Main menu |
| `/atm/session/balance` | Customer | Check balance |
| `/atm/session/withdraw` | Customer | Cash withdrawal |
| `/atm/session/deposit` | Customer | Cash/cheque deposit |
| `/atm/session/transfer` | Customer | Fund transfer |
| `/atm/session/bills` | Customer | Bill payment |
| `/atm/session/history` | Customer | Transaction history |
| `/atm/session/receipt` | Customer | Last transaction receipt |
| `/staff/login` | Staff | Staff login page |
| `/staff/manager/dashboard` | Manager | Manager home |
| `/staff/manager/approvals` | Manager | Pending large transactions |
| `/staff/manager/audit` | Manager | Full audit log |
| `/staff/admin/dashboard` | Admin | Admin home |
| `/staff/admin/diagnostics` | Admin | Hardware diagnostics report |
| `/staff/admin/firmware` | Admin | Update firmware version |
| `/staff/admin/cards` | Admin | Card management (retain/release) |
| `/h2-console` | Dev | H2 database console |

---

## Project Structure

```
src/main/java/com/atm/
├── AtmApplication.java              # Spring Boot entry point
├── config/
│   ├── SecurityConfig.java          # Dual filter chain (staff + customer)
│   ├── WebConfig.java               # Interceptor registration
│   └── DataInitializer.java         # Seeds all test data on startup
├── controller/
│   ├── AuthController.java          # Card insert, PIN entry, logout
│   ├── CustomerController.java      # All transaction flows
│   ├── ManagerController.java       # Approval and audit flows
│   └── AdminController.java         # Diagnostics, firmware, cards
├── service/
│   ├── AuthenticationService.java   # Card auth, PIN verify, session lifecycle
│   ├── TransactionService.java      # Facade — all 4 transaction types
│   ├── ManagerService.java          # Approval / audit queries
│   ├── AdminService.java            # Diagnostics, firmware, card management
│   └── ATMUserDetailsService.java   # Spring Security UserDetailsService
├── service/external/
│   ├── BankCoreBankingService.java  # Core banking interface (DIP)
│   └── ExternalCardNetworkService.java  # Card network interface (DIP)
├── model/
│   ├── ATMUser.java                 # Abstract base — Customer, Manager, Admin (OCP + LSP)
│   ├── Customer.java
│   ├── BankManager.java
│   ├── SystemAdministrator.java
│   ├── Account.java
│   ├── ATMCard.java
│   ├── ATMSession.java
│   ├── Transaction.java             # Abstract base — all 4 types (OCP + LSP + Template Method)
│   ├── WithdrawTransaction.java
│   ├── DepositTransaction.java
│   ├── TransferTransaction.java
│   ├── BillPayment.java
│   └── enums/                       # AccountType, SessionState, TransactionStatus, UserRole
├── repository/                      # 7 focused Spring Data JPA interfaces (ISP)
│   ├── CustomerRepository.java
│   ├── AccountRepository.java
│   ├── TransactionRepository.java
│   ├── ATMSessionRepository.java
│   ├── ATMCardRepository.java
│   ├── BankManagerRepository.java
│   └── SystemAdministratorRepository.java
├── factory/
│   └── TransactionFactory.java      # Factory Pattern — creates all 4 transaction types
├── hardware/
│   └── ATMHardwareController.java   # Singleton — cash dispenser + card reader simulation
├── dto/
│   └── DiagnosticsReport.java       # Builder Pattern — 10-field diagnostics report
└── interceptor/
    └── CustomerSessionInterceptor.java  # Guards all /atm/session/* routes

src/main/resources/
├── application.properties
├── templates/
│   ├── auth/        # insert-card, pin-entry, card-retained
│   ├── customer/    # dashboard, balance, withdraw, deposit, transfer, bills, receipt, history
│   ├── manager/     # dashboard, approvals, audit
│   ├── admin/       # dashboard, diagnostics, firmware, retained-cards
│   └── layout/      # base layout fragments
└── static/css/      # Stylesheets
```

---

## Architecture

### MVC

```
Browser
  └─► Controller (@Controller)
          └─► Service (business logic)
                  ├─► Repository (Spring Data JPA → H2)
                  └─► External Services (card network, core banking)
  ◄── View (Thymeleaf templates)
```

### Security — Dual Filter Chain

```
/staff/**  ──► Spring Security form login (MGR001, ADM001)
/atm/**    ──► CustomerSessionInterceptor (card + PIN session)
```

---

## Design Principles (SOLID)

| Principle | Where |
|-----------|-------|
| **SRP** — Single Responsibility | Each service has exactly one job: `TransactionService` ≠ `AuthenticationService` ≠ `ManagerService` |
| **OCP** — Open/Closed | `Transaction` and `ATMUser` are abstract; new types added by subclassing, never by editing the base |
| **LSP** — Liskov Substitution | All `Transaction` subtypes substitute safely — Manager page holds a `Transaction` reference and calls `getAmount()`, `getType()` on any subtype |
| **ISP** — Interface Segregation | 7 focused repository interfaces — `AuthenticationService` imports `CustomerRepository` only, never sees `TransactionRepository` methods |
| **DIP** — Dependency Inversion | All `@Autowired` fields in services are interfaces; Spring injects concrete beans at runtime |

---

## Design Patterns

| Pattern | Category | File | Summary |
|---------|----------|------|---------|
| **Facade** | Structural | `TransactionService.java` | 4 simple public methods hide 9-step internal workflow |
| **Singleton** | Creational | `ATMHardwareController.java` | Double-checked locking — one hardware instance for the one physical ATM |
| **Factory** | Creational | `TransactionFactory.java` | Centralises creation of all 4 transaction types; removes 20 lines of boilerplate |
| **Builder** | Creational | `DiagnosticsReport.java` | Fluent chain builds a 10-field immutable diagnostics report |
| **Strategy** | Behavioural | `Transaction` subclasses | Each subclass implements `execute()` differently — swap behaviour by swapping object |
| **Template Method** | Behavioural | `Transaction.java` | Abstract `execute()` defines the skeleton; subclasses fill in the steps |
| **Interceptor** | Behavioural | `CustomerSessionInterceptor.java` | Guards every `/atm/session/*` route before the controller runs |

---

## Business Rules

| Rule | Value | Config key |
|------|-------|-----------|
| Large transaction threshold | $5,000 | `atm.transaction.large-threshold` |
| Max PIN attempts before retention | 3 | `atm.security.max-pin-attempts` |
| Session timeout | 10 minutes | `server.servlet.session.timeout` |

---

## H2 Console

Available at **http://localhost:8080/h2-console** while the app is running.

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/atmdb` |
| Username | `sa` |
| Password | *(leave blank)* |

Useful queries:
```sql
SELECT * FROM CUSTOMER;
SELECT * FROM TRANSACTION ORDER BY TIMESTAMP DESC;
SELECT card_number, is_retained, failed_attempts FROM ATM_CARD;
SELECT * FROM ATM_SESSION WHERE is_authenticated = TRUE;
```

---

## Key Test Scenarios

1. **Normal withdrawal** — Login as Alice (`4111111111111111` / `1234`), withdraw $200 → success
2. **Large transaction approval** — Withdraw $6,000 → status becomes `AWAITING_APPROVAL`; login as Manager → approve/reject
3. **Card retention** — Enter wrong PIN 3 times → card retained; login as Admin → release card
4. **Low balance** — Login as Carol (`4000000000000002` / `9999`), try to withdraw $500 → insufficient funds
5. **Fund transfer** — Transfer between two active accounts; verify both balances update
6. **Admin diagnostics** — Login as Admin → run diagnostics → see live hardware status + session count
