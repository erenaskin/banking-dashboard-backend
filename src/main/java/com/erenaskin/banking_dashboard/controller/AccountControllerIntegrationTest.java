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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private String testIban;

    @BeforeEach
    void setup() throws Exception {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        userRepository.save(user);

        String loginPayload = """
                {
                    "email": "test@example.com",
                    "password": "12345678"
                }
                """;

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @Order(1)
    void createAccount_ShouldReturnOk() throws Exception {
        AccountRequest request = new AccountRequest("TRY");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void getUserAccounts_ShouldReturnArrayAndSaveIban() throws Exception {
        createAccount_ShouldReturnOk(); // ensure account exists

        String response = mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        testIban = json.get(0).get("iban").asText();
        assertThat(testIban).startsWith("TR");
    }

    @Test
    @Order(3)
    void getCurrencyByIban_ShouldReturnTRY() throws Exception {
        getUserAccounts_ShouldReturnArrayAndSaveIban();

        mockMvc.perform(get("/api/accounts/{iban}/currency", testIban)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("TRY"));
    }

    @Test
    @Order(4)
    void depositToAccount_ShouldSucceed() throws Exception {
        getUserAccounts_ShouldReturnArrayAndSaveIban();

        TransferRequest request = new TransferRequest();
        request.setType("DEPOSIT");
        request.setAmount(new BigDecimal("500.00"));
        request.setReceiverIban(testIban);
        request.setSenderIban(testIban);

        mockMvc.perform(post("/api/accounts/{iban}/transactions", testIban)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void getAccountDetails_ShouldIncludeTransactions() throws Exception {
        depositToAccount_ShouldSucceed();

        mockMvc.perform(get("/api/accounts/{iban}/details", testIban)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(testIban))
                .andExpect(jsonPath("$.balance").value("500.00"))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].amount").value("500.00"))
                .andExpect(jsonPath("$.transactions[0].type").value("DEPOSIT"));
    }
}
