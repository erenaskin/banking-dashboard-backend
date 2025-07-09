package com.erenaskin.banking_dashboard.impl;

import com.erenaskin.banking_dashboard.dto.*;
import com.erenaskin.banking_dashboard.entity.*;
import com.erenaskin.banking_dashboard.entity.Currency;
import com.erenaskin.banking_dashboard.mapper.AccountMapper;
import com.erenaskin.banking_dashboard.mapper.TransactionMapper;
import com.erenaskin.banking_dashboard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountMapper accountMapper;
    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountServiceImpl accountService;

    private final String USER_EMAIL = "test@example.com";
    private User user;

    @BeforeEach
    void setUp() {
        // SecurityContextHolder mocklama
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(USER_EMAIL);

        // User mock objesi
        user = User.builder().id(1L).email(USER_EMAIL).build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void createAccount_ShouldSaveNewAccount() {
        AccountRequest request = new AccountRequest("TRY");

        // save çağrısını yakalayıp validasyon yapabiliriz
        doAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            assertNotNull(acc.getIban());
            assertEquals(BigDecimal.ZERO, acc.getBalance());
            assertEquals("TRY", acc.getCurrency());
            assertEquals(user, acc.getUser());
            assertNotNull(acc.getCreatedAt());
            return acc;
        }).when(accountRepository).save(any(Account.class));

        accountService.createAccount(request);

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void getUserAccounts_ShouldReturnMappedAccounts() {
        Account acc1 = Account.builder().iban("TR123").user(user).build();
        Account acc2 = Account.builder().iban("TR456").user(user).build();
        List<Account> accounts = List.of(acc1, acc2);

        when(accountRepository.findByUser(user)).thenReturn(accounts);
        when(accountMapper.toResponse(acc1)).thenReturn(new AccountResponse("TR123", BigDecimal.ZERO, Currency.TRY));
        when(accountMapper.toResponse(acc2)).thenReturn(new AccountResponse("TR456", BigDecimal.ZERO, Currency.USD));


        List<AccountResponse> responses = accountService.getUserAccounts();

        assertEquals(2, responses.size());
        assertEquals("TR123", responses.get(0).iban());
        assertEquals("TR456", responses.get(1).iban());

        verify(accountRepository, times(1)).findByUser(user);
        verify(accountMapper, times(2)).toResponse(any());
    }

}

