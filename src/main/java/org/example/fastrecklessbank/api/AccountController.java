package org.example.fastrecklessbank.api;

import jakarta.validation.Valid;
import org.example.fastrecklessbank.api.dto.AccountResponse;
import org.example.fastrecklessbank.api.dto.CreateAccountRequest;
import org.example.fastrecklessbank.api.dto.MoneyOperationRequest;
import org.example.fastrecklessbank.api.dto.TransferRecordResponse;
import org.example.fastrecklessbank.application.BankService;
import org.example.fastrecklessbank.domain.Account;
import org.example.fastrecklessbank.domain.AccountId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final BankService bankService;

    public AccountController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        Account account = bankService.createAccount(
                request.name(), request.surname(), request.initialDepositOrZero());
        return AccountResponse.from(account);
    }

    @GetMapping
    public List<AccountResponse> list() {
        return bankService.listAccounts().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(bankService.requireAccount(new AccountId(id)));
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable UUID id,
                                   @Valid @RequestBody MoneyOperationRequest request) {
        return AccountResponse.from(bankService.deposit(new AccountId(id), request.amountCents()));
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable UUID id,
                                    @Valid @RequestBody MoneyOperationRequest request) {
        return AccountResponse.from(bankService.withdraw(new AccountId(id), request.amountCents()));
    }

    @GetMapping("/{id}/transfers")
    public List<TransferRecordResponse> transfers(@PathVariable UUID id) {
        return bankService.getRecentTransfers(new AccountId(id)).stream()
                .map(TransferRecordResponse::from)
                .toList();
    }
}
