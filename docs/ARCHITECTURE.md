# Fast & Reckless Bank — Architecture

A layered architecture with a domain model: the layers are preserved, but business
logic lives partly in the domain classes. `BankService` orchestrates operations
rather than doing everything itself.

```
React Frontend        (planned)
      |
   api layer
      |
application layer
      |
  domain layer
      |
infrastructure layer
```

## Layers

- **api** — REST layer. Accepts HTTP requests from the frontend, calls the
  application layer, and returns HTTP responses. Always exchanges DTOs, never
  domain objects.
- **application** — executes the bank's use cases: create account, deposit,
  withdraw, transfer. Depends only on the repository *ports*, not on concrete
  storage.
- **domain** — core business entities and rules (`Account`, `Money`, …). Has no
  dependency on Spring, REST, a database, or the frontend.
- **infrastructure** — concrete storage. Currently in-memory; a database could be
  added later behind the same ports.
- **common** — cross-cutting types, e.g. domain exceptions.

## Package layout

```
org.example.fastrecklessbank
  api
    AccountController          # create / list / get / deposit / withdraw / transfers history
    TransferController         # POST /api/transfers
    ApiExceptionHandler        # maps exceptions -> HTTP status + ErrorResponse
    dto
      CreateAccountRequest     # name, surname, optional initialDepositCents
      MoneyOperationRequest    # amountCents (deposit / withdraw)
      TransferRequest          # fromAccountId, toAccountId, amountCents
      AccountResponse
      TransferRecordResponse
      ErrorResponse

  application
    BankService
    port
      AccountRepository
      TransferHistoryRepository

  domain
    Account                    # id, name, surname, balance, per-account lock
    AccountId
    Money                      # amount in cents (long)
    TransferRecord

  infrastructure
    InMemoryAccountRepository
    InMemoryTransferHistoryRepository
    TransferStorage            # per-account fixed-capacity (50) ring buffer

  common
    exception
      AccountNotFoundException
      InsufficientFundsException
      InvalidAmountException
```

> Note: the last-50 outgoing-transfer ring buffer (`TransferStorage`) is a storage
> / memory-efficiency concern, so it lives in `infrastructure`, not `domain`.

## Rules

- Money is stored as a `long` number of cents.
- A transfer is an atomic operation.
- Locking is per account, never a single global bank lock.
- To avoid deadlocks, transfers acquire both account locks in the same order,
  sorted by `AccountId`.
- The API returns DTOs, never domain objects.
- Amounts are validated: `0`, negative, non-numeric, and `null` are rejected.
- A transfer from an account to itself is rejected.
- Each account keeps its last 50 outgoing transfers, in-memory, in a fast and
  memory-efficient ring buffer.

## Tests

JUnit 5 + AssertJ. Coverage includes domain rules (`Money`, `Account`), the ring
buffer (`TransferStorage`), `BankService` (including concurrent transfers that
conserve total balance and run deadlock-free), and end-to-end API tests via
`MockMvc`.
