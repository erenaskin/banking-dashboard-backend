package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.TransactionResponse;
import com.erenaskin.banking_dashboard.dto.TransferRequest;
import com.erenaskin.banking_dashboard.entity.Role;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionServiceIntegrationTest {

    @Autowired private TransactionService transactionService;
    @Autowired private AccountService accountService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static String iban;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, List.of()
                )
        );

        accountService.createAccount(new com.erenaskin.banking_dashboard.dto.AccountRequest("TRY"));
        iban = accountService.getUserAccounts().get(0).iban();
    }

    @Test
    @Order(1)
    void transfer_ShouldSucceed() {
        TransferRequest deposit = new TransferRequest();
        deposit.setType("DEPOSIT");
        deposit.setAmount(new BigDecimal("1500"));
        deposit.setReceiverIban(iban);
        deposit.setSenderIban(iban);

        transactionService.transfer(deposit);

        List<TransactionResponse> history = transactionService.getHistory(iban);
        assertEquals(1, history.size());
        assertEquals(new BigDecimal("1500"), history.get(0).getAmount());
    }
}
