package com.erenaskin.banking_dashboard.dto;

import com.erenaskin.banking_dashboard.entity.Transaction;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AccountDetailsResponse(
        String iban,
        String currency,
        BigDecimal balance,
        List<TransactionResponse> transactions
) {}
