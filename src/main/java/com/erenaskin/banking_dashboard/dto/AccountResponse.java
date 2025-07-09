package com.erenaskin.banking_dashboard.dto;

import com.erenaskin.banking_dashboard.entity.Currency;

import java.math.BigDecimal;

public record AccountResponse(
        String iban,
        BigDecimal balance,
        Currency currency
) {}
