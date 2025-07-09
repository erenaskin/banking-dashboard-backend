package com.erenaskin.banking_dashboard.controller;

import com.erenaskin.banking_dashboard.dto.AccountRequest;
import com.erenaskin.banking_dashboard.dto.AccountResponse;
import com.erenaskin.banking_dashboard.entity.Currency;
import com.erenaskin.banking_dashboard.service.AccountService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(AccountControllerUnitTest.TestConfig.class)
public class AccountControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountService accountService; // Bu mocku manuel config’den alıyoruz

    @Test
    void createAccount_ShouldReturnOk() throws Exception {
        AccountRequest request = new AccountRequest("TRY");

        // createAccount void method, sadece verify için mockla
        Mockito.doNothing().when(accountService).createAccount(any(AccountRequest.class));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserAccounts_ShouldReturnList() throws Exception {
        List<AccountResponse> mockAccounts = List.of(
                new AccountResponse("TR123", BigDecimal.valueOf(1000), Currency.TRY),
                new AccountResponse("TR456", BigDecimal.valueOf(2000), Currency.EUR)
        );

        when(accountService.getUserAccounts()).thenReturn(mockAccounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].iban").value("TR123"))
                .andExpect(jsonPath("$[0].balance").value(1000))
                .andExpect(jsonPath("$[0].currency").value("TRY"))
                .andExpect(jsonPath("$[1].iban").value("TR456"))
                .andExpect(jsonPath("$[1].balance").value(2000))
                .andExpect(jsonPath("$[1].currency").value("EUR"));
    }

    @Test
    void getCurrencyByIban_ShouldReturnCurrencyString() throws Exception {
        String iban = "TR123";
        when(accountService.getCurrencyByIban(iban)).thenReturn(Currency.TRY);

        mockMvc.perform(get("/api/accounts/{iban}/currency", iban))
                .andExpect(status().isOk())
                .andExpect(content().string("TRY"));
    }

    @Test
    void getAccountDetails_ShouldReturnAccountDetailsResponse() throws Exception {
        String iban = "TR123";
        var accountDetails = new com.erenaskin.banking_dashboard.dto.AccountDetailsResponse(
                iban,
                "TRY",
                BigDecimal.valueOf(1500),
                List.of()
        );

        when(accountService.getAccountDetails(iban)).thenReturn(accountDetails);

        mockMvc.perform(get("/api/accounts/{iban}/details", iban))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.currency").value("TRY"))
                .andExpect(jsonPath("$.balance").value(1500))
                .andExpect(jsonPath("$.transactions").isArray());
    }

    @Test
    void createTransaction_ShouldReturnOk() throws Exception {
        String iban = "TR123";
        var transferRequest = new com.erenaskin.banking_dashboard.dto.TransferRequest();
        transferRequest.setType("TRANSFER");
        transferRequest.setAmount(BigDecimal.valueOf(300));
        transferRequest.setSenderIban(iban);
        transferRequest.setReceiverIban("TR456");

        // createTransaction void method
        Mockito.doNothing().when(accountService).createTransaction(Mockito.eq(iban), Mockito.any());

        mockMvc.perform(post("/api/accounts/{iban}/transactions", iban)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void createAccount_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Örneğin currency boş bırakılırsa (dto validasyon varsa)
        AccountRequest invalidRequest = new AccountRequest(null);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Configuration
    static class TestConfig {
        @Bean
        public AccountService accountService() {
            return Mockito.mock(AccountService.class);
        }
    }
}
