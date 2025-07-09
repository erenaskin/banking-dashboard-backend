package com.erenaskin.banking_dashboard.controller;

import com.erenaskin.banking_dashboard.dto.TransactionResponse;
import com.erenaskin.banking_dashboard.dto.TransferRequest;
import com.erenaskin.banking_dashboard.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerUnitTest.TestConfig.class)
public class TransactionControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionService transactionService;

    @Test
    void transfer_ShouldReturnOk() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setType("TRANSFER");
        request.setAmount(BigDecimal.valueOf(100));
        request.setSenderIban("TR123");
        request.setReceiverIban("TR456");

        doNothing().when(transactionService).transfer(any(TransferRequest.class));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void history_ShouldReturnTransactionList() throws Exception {
        String iban = "TR123";
        List<TransactionResponse> transactions = List.of(
                new TransactionResponse("TR123", "TR456", BigDecimal.valueOf(100), LocalDateTime.of(2025, 7, 9, 12, 0)),
                new TransactionResponse("TR789", "TR123", BigDecimal.valueOf(50), LocalDateTime.of(2025, 7, 8, 9, 30))
        );

        when(transactionService.getHistory(iban)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/{iban}", iban))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderIban").value(iban))
                .andExpect(jsonPath("$[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$[1].receiverIban").value(iban))
                .andExpect(jsonPath("$[1].type").value("DEPOSIT"));
    }

    @Configuration
    static class TestConfig {
        @Bean
        public TransactionService transactionService() {
            return Mockito.mock(TransactionService.class);
        }
    }
}
