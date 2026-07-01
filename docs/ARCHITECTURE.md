Layered Architecture + Domain Model 2:

Слои остаются, но бизнес-логика частично живет в domain-классах.
То есть BankService не делает всё сам, а оркестрирует операции.

```angular2html
React Frontend
     |
api layer
     |
application layer
     |
domain layer
     |
infrastructure layer
```

```angular2html
org.example.fastrecklessbank
  api
    AccountController
    TransferController
    ApiExceptionHandler
    dto
      CreateAccountRequest
      MoneyOperationRequest
      TransferRequest
      AccountResponse
      TransferRecordResponse
      ErrorResponse

  application
    BankService
    port
      AccountRepository
      TransferHistoryRepository

  domain
    Account
    AccountId
    Money
    TransferRecord
    TransferHistory

  infrastructure
    inmemory
      InMemoryAccountRepository
      InMemoryTransferHistoryRepository

  common
    exception
      AccountNotFoundException
      InsufficientFundsException
      InvalidAmountException
```

api - слой REST API. Принимает HTTP-запросы от frontend, вызывает application layer и возвращает HTTP-ответы.
application - За что отвечает: выполняет бизнес-операции приложения: создать аккаунт, пополнить, снять, перевести деньги.
domain - За что отвечает: хранит основные бизнес-сущности и правила. Не зависит от Spring, REST, базы данных или frontend.
infrastructure - За что отвечает: конкретное хранение данных. Сейчас in-memory, позже можно добавить database.
common - common layer

Tests:
JUnit 5 + AssertJ

Rules:
- Save money in long 
- 