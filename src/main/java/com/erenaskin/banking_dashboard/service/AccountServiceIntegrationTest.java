package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.AccountDetailsResponse;
import com.erenaskin.banking_dashboard.dto.AccountRequest;
import com.erenaskin.banking_dashboard.dto.AccountResponse;
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
class AccountServiceIntegrationTest {

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

        // Güvenlik bağlamı simülasyonu
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, List.of()
                )
        );
    }

    @Test
    @Order(1)
    void createAccount_ShouldSucceed() {
        accountService.createAccount(new AccountRequest("TRY"));
        List<AccountResponse> accounts = accountService.getUserAccounts();
        assertEquals(1, accounts.size());
        iban = accounts.get(0).iban();
        assertTrue(iban.startsWith("TR"));
    }

    @Test
    @Order(2)
    void deposit_ShouldIncreaseBalance() {
        createAccount_ShouldSucceed();

        TransferRequest deposit = new TransferRequest();
        deposit.setType("DEPOSIT");
        deposit.setAmount(new BigDecimal("1000"));
        deposit.setSenderIban(iban);
        deposit.setReceiverIban(iban);

        accountService.createTransaction(iban, deposit);

        AccountDetailsResponse details = accountService.getAccountDetails(iban);
        assertEquals(new BigDecimal("1000"), details.balance());
    }
}
