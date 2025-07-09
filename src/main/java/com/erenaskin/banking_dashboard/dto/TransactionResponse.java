package com.erenaskin.banking_dashboard.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String senderIban;
    private String receiverIban;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
