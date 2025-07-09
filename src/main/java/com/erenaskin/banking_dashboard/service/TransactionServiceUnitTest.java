package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.TransactionResponse;
import com.erenaskin.banking_dashboard.dto.TransferRequest;
import com.erenaskin.banking_dashboard.entity.Account;
import com.erenaskin.banking_dashboard.entity.Transaction;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.mapper.TransactionMapper;
import com.erenaskin.banking_dashboard.repository.AccountRepository;
import com.erenaskin.banking_dashboard.repository.TransactionRepository;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import com.erenaskin.banking_dashboard.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceUnitTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void transfer_ShouldThrow_WhenUserNotFound() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR123", "TR456");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.transfer(request));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void transfer_ShouldThrow_WhenSenderIbanNotOwnedByUser() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR123", "TR456");
        User user = new User();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(Account.builder().iban("TR000").build()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.transfer(request));
        assertEquals("Sender IBAN not found or not owned by user", ex.getMessage());
    }

    @Test
    void transfer_ShouldThrow_WhenReceiverIbanNotFound() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR123", "TR456");
        User user = new User();
        Account sender = Account.builder().iban("TR123").balance(new BigDecimal("200")).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(sender));
        when(accountRepository.findAll()).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.transfer(request));
        assertEquals("Receiver IBAN not found", ex.getMessage());
    }

    @Test
    void transfer_ShouldThrow_WhenInsufficientBalance() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("300"), "TR123", "TR456");
        User user = new User();
        Account sender = Account.builder().iban("TR123").balance(new BigDecimal("200")).build();
        Account receiver = Account.builder().iban("TR456").balance(new BigDecimal("100")).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(sender));
        when(accountRepository.findAll()).thenReturn(List.of(receiver));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.transfer(request));
        assertEquals("Insufficient balance", ex.getMessage());
    }

    @Test
    void getHistory_ShouldReturnMappedTransactions() {
        String iban = "TR123";
        Transaction tx = Transaction.builder().id(1L).amount(new BigDecimal("100")).senderIban(iban).build();

        when(transactionRepository.findBySenderIbanOrReceiverIban(iban, iban)).thenReturn(List.of(tx));
        when(transactionMapper.toResponse(tx)).thenReturn(
                new TransactionResponse("TR123", "TR456", new BigDecimal("100"), null)
        );

        List<TransactionResponse> responses = transactionService.getHistory(iban);

        assertEquals(1, responses.size());
        verify(transactionMapper).toResponse(tx);
    }
}
