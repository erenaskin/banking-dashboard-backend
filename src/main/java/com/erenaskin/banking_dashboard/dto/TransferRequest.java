package com.erenaskin.banking_dashboard.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Type is mandatory")
    private String type;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Receiver IBAN is mandatory")
    private String receiverIban;

    @NotBlank(message = "Sender IBAN is mandatory")
    private String senderIban;
}
