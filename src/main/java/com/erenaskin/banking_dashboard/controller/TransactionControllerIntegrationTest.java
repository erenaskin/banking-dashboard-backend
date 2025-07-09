package com.erenaskin.banking_dashboard.controller;

import com.erenaskin.banking_dashboard.dto.AccountRequest;
import com.erenaskin.banking_dashboard.dto.TransferRequest;
import com.erenaskin.banking_dashboard.entity.Role;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.repository.AccountRepository;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private String senderIban;
    private String receiverIban;

    @BeforeEach
    void setup() throws Exception {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setFullName("Transaction User");
        user.setEmail("txn@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        // Giriş yap ve JWT al
        String loginPayload = """
                {
                  "email": "txn@example.com",
                  "password": "password123"
                }
                """;

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        jwtToken = objectMapper.readTree(loginResponse).get("token").asText();

        // 2 hesap oluştur
        createAccount("TRY");
        createAccount("TRY");

        List<String> ibans = fetchIbans();
        senderIban = ibans.get(0);
        receiverIban = ibans.get(1);

        // Sender hesabına para yatır
        deposit(senderIban, new BigDecimal("1000.00"));
    }

    void createAccount(String currency) throws Exception {
        AccountRequest request = new AccountRequest(currency);
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    List<String> fetchIbans() throws Exception {
        String json = mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        return List.of(
                array.get(0).get("iban").asText(),
                array.get(1).get("iban").asText()
        );
    }

    void deposit(String iban, BigDecimal amount) throws Exception {
        TransferRequest deposit = new TransferRequest();
        deposit.setType("DEPOSIT");
        deposit.setAmount(amount);
        deposit.setSenderIban(iban);
        deposit.setReceiverIban(iban);

        mockMvc.perform(post("/api/accounts/{iban}/transactions", iban)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    void transfer_ShouldSucceed_WhenValid() throws Exception {
        TransferRequest transfer = new TransferRequest();
        transfer.setType("TRANSFER");
        transfer.setAmount(new BigDecimal("250.00"));
        transfer.setSenderIban(senderIban);
        transfer.setReceiverIban(receiverIban);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void transactionHistory_ShouldIncludeTransfer() throws Exception {
        transfer_ShouldSucceed_WhenValid();

        String json = mockMvc.perform(get("/api/transactions/{iban}", senderIban)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertThat(array.size()).isGreaterThan(0);
        assertThat(array.get(0).get("senderIban").asText()).isEqualTo(senderIban);
        assertThat(array.get(0).get("receiverIban").asText()).isEqualTo(receiverIban);
        assertThat(array.get(0).get("amount").asText()).isEqualTo("250.00");
    }
}
