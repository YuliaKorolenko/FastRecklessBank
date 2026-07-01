# Fast & Reckless Bank

A Spring Boot (Java 21) REST backend plus a React frontend for the bank's core
operations: **create account, deposit, withdraw, transfer**, and a view of each
account's **last 50 outgoing transfers**.

All data is held **in-memory**.
See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the design and rules.

## Prerequisites

- **JDK 21**
- **Node 18+ / npm** (for the frontend)

## Run the backend

From the project root:

```bash
./gradlew bootRun
```

The API starts on **http://localhost:8080**.

## Run the frontend

The frontend is a standalone React (Vite) app. In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. The Vite dev server proxies `/api` calls to the
backend on `:8080`, so no CORS setup is needed — just keep `bootRun` running in
the other terminal.

To build and preview the production bundle instead:

```bash
cd frontend
npm run build     # outputs frontend/dist
npm run preview   # serves the build on http://localhost:4173 (also proxies /api)
```

## Run the tests

```bash
./gradlew test
```

Covers the domain rules, the ring buffer (including wraparound and concurrent
access), `BankService` (including concurrent transfers that conserve total
balance and run deadlock-free), and the REST API end-to-end via `MockMvc`.

## API reference

Amounts are always in **cents** (e.g. `10000` = $100.00).

| Method | Path | Body | Result |
|---|---|---|---|
| POST | `/api/accounts` | `{ "name", "surname", "initialDepositCents"? }` | `201` account |
| GET | `/api/accounts` | — | account list |
| GET | `/api/accounts/{id}` | — | account |
| POST | `/api/accounts/{id}/deposit` | `{ "amountCents" }` | updated account |
| POST | `/api/accounts/{id}/withdraw` | `{ "amountCents" }` | updated account |
| GET | `/api/accounts/{id}/transfers` | — | last 50 outgoing (newest first) |
| POST | `/api/transfers` | `{ "fromAccountId", "toAccountId", "amountCents" }` | updated source account |

Errors return `{ "code", "message" }` with status `400` (invalid amount,
validation, self-transfer), `404` (unknown account), or `409` (insufficient funds).

Example:

```bash
curl -X POST http://localhost:8080/api/accounts \
     -H 'Content-Type: application/json' \
     -d '{"name":"Alice","surname":"Smith","initialDepositCents":10000}'
```
