package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.TransactionResponse;
import com.erenaskin.banking_dashboard.dto.TransferRequest;

import java.util.List;

public interface TransactionService {
    void transfer(TransferRequest request);
    List<TransactionResponse> getHistory(String iban);
}
