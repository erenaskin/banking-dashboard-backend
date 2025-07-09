package com.erenaskin.banking_dashboard.controller;

import com.erenaskin.banking_dashboard.dto.*;
import com.erenaskin.banking_dashboard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request) {
        transactionService.transfer(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{iban}")
    public ResponseEntity<List<TransactionResponse>> history(@PathVariable String iban) {
        return ResponseEntity.ok(transactionService.getHistory(iban));
    }
}
