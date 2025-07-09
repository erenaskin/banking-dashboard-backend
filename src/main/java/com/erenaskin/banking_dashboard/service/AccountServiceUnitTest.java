package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.*;
import com.erenaskin.banking_dashboard.entity.*;
import com.erenaskin.banking_dashboard.entity.Currency;
import com.erenaskin.banking_dashboard.impl.AccountServiceImpl;
import com.erenaskin.banking_dashboard.mapper.AccountMapper;
import com.erenaskin.banking_dashboard.repository.*;
import com.erenaskin.banking_dashboard.util.IbanGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceUnitTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private IbanGenerator ibanGenerator;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setEmail("user@example.com");
    }

    @Test
    void createAccount_ShouldSaveWithIban() {
        AccountRequest request = new AccountRequest("TRY");
        Account account = new Account();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(ibanGenerator.generateIban()).thenReturn("TR123");

        accountService.createAccount(request);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getUserAccounts_ShouldReturnList() {
        Account acc = new Account();
        acc.setIban("TR123");
        acc.setBalance(BigDecimal.TEN);
        acc.setCurrency(Currency.TRY);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUser(testUser)).thenReturn(List.of(acc));
        when(accountMapper.toResponse(acc)).thenReturn(new AccountResponse("TR123", BigDecimal.TEN, Currency.TRY));

        List<AccountResponse> result = accountService.getUserAccounts();

        assertEquals(1, result.size());
        assertEquals("TR123", result.get(0).iban());
    }

    @Test
    void getCurrencyByIban_ShouldReturnCurrency() {
        Account acc = new Account();
        acc.setCurrency(Currency.USD);
        when(accountRepository.findByIban("TR123")).thenReturn(Optional.of(acc));

        Currency result = accountService.getCurrencyByIban("TR123");

        assertEquals(Currency.USD, result);
    }

    @Test
    void getAccountDetails_ShouldReturnDetails() {
        Account acc = new Account();
        acc.setIban("TR123");
        acc.setBalance(BigDecimal.valueOf(100));
        acc.setCurrency(Currency.EUR);
        when(accountRepository.findByIban("TR123")).thenReturn(Optional.of(acc));
        when(transactionRepository.findBySenderIbanOrReceiverIban("TR123", "TR123")).thenReturn(List.of());
        AccountDetailsResponse.builder()
                .iban("TR123")
                .currency("TRY")
                .balance(BigDecimal.TEN)
                .transactions(List.of())
                .build();
        AccountDetailsResponse result = accountService.getAccountDetails("TR123");

        assertEquals("TR123", result.iban());
        assertEquals("EUR", result.currency());
    }

    @Test
    void createAccount_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.createAccount(new AccountRequest("TRY")));

        assertEquals("User not found", ex.getMessage());
    }
}
