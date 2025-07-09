package com.erenaskin.banking_dashboard.controller;

import com.erenaskin.banking_dashboard.dto.*;
import com.erenaskin.banking_dashboard.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody AccountRequest request) {
        accountService.createAccount(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts() {
        List<AccountResponse> accounts = accountService.getUserAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{iban}/currency")
    public ResponseEntity<String> getCurrency(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getCurrencyByIban(iban).name());
    }

    @GetMapping("/{iban}/details")
    public ResponseEntity<AccountDetailsResponse> getAccountDetails(@PathVariable String iban) {
        AccountDetailsResponse details = accountService.getAccountDetails(iban);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/{iban}/transactions")
    public ResponseEntity<Void> createTransaction(@PathVariable String iban, @RequestBody TransferRequest request) {
        accountService.createTransaction(iban, request);
        return ResponseEntity.ok().build();
    }
}
