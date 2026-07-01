package org.example.fastrecklessbank.api;

import jakarta.validation.Valid;
import org.example.fastrecklessbank.api.dto.AccountResponse;
import org.example.fastrecklessbank.api.dto.TransferRequest;
import org.example.fastrecklessbank.application.BankService;
import org.example.fastrecklessbank.domain.Account;
import org.example.fastrecklessbank.domain.AccountId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final BankService bankService;

    public TransferController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    public AccountResponse transfer(@Valid @RequestBody TransferRequest request) {
        Account from = bankService.transfer(
                new AccountId(request.fromAccountId()),
                new AccountId(request.toAccountId()),
                request.amountCents());
        return AccountResponse.from(from);
    }
}
