package com.erenaskin.banking_dashboard.impl;

import com.erenaskin.banking_dashboard.dto.TransactionResponse;
import com.erenaskin.banking_dashboard.dto.TransferRequest;
import com.erenaskin.banking_dashboard.entity.Account;
import com.erenaskin.banking_dashboard.entity.Transaction;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.mapper.TransactionMapper;
import com.erenaskin.banking_dashboard.repository.AccountRepository;
import com.erenaskin.banking_dashboard.repository.TransactionRepository;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Spring Security context
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void transfer_ShouldThrow_WhenUserNotFound() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR456", "TR123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.transfer(request));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void transfer_ShouldThrow_WhenSenderIbanNotOwnedByUser() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR456", "TR123");

        User user = new User();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(
                Account.builder().iban("TR999").balance(BigDecimal.TEN).build()
        ));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.transfer(request));
        assertEquals("Sender IBAN not found or not owned by user", ex.getMessage());
    }

    @Test
    void transfer_ShouldThrow_WhenReceiverIbanNotFound() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR456", "TR123");

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
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("300"), "TR456", "TR123");

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
    @Transactional
    void transfer_ShouldUpdateBalancesAndSaveTransaction() {
        TransferRequest request = new TransferRequest("TRANSFER", new BigDecimal("100"), "TR456", "TR123");

        User user = new User();
        Account sender = Account.builder().iban("TR123").balance(new BigDecimal("200")).build();
        Account receiver = Account.builder().iban("TR456").balance(new BigDecimal("100")).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(sender));
        when(accountRepository.findAll()).thenReturn(List.of(receiver));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.transfer(request);

        assertEquals(new BigDecimal("100"), sender.getBalance());
        assertEquals(new BigDecimal("200"), receiver.getBalance());

        verify(accountRepository).save(sender);
        verify(accountRepository).save(receiver);
        verify(transactionRepository).save(any());
    }

    @Test
    void getHistory_ShouldReturnMappedTransactions() {
        String iban = "TR123";
        Transaction tx1 = Transaction.builder().id(1L).senderIban(iban).amount(new BigDecimal("50")).build();
        Transaction tx2 = Transaction.builder().id(2L).receiverIban(iban).amount(new BigDecimal("30")).build();

        when(transactionRepository.findBySenderIbanOrReceiverIban(iban, iban)).thenReturn(List.of(tx1, tx2));
        when(transactionMapper.toResponse(tx1)).thenReturn(new TransactionResponse(iban, null, new BigDecimal("50"), null));
        when(transactionMapper.toResponse(tx2)).thenReturn(new TransactionResponse(null, iban, new BigDecimal("30"), null));

        List<TransactionResponse> responses = transactionService.getHistory(iban);

        assertEquals(2, responses.size());
        verify(transactionRepository).findBySenderIbanOrReceiverIban(iban, iban);
        verify(transactionMapper, times(2)).toResponse(any());
    }
}
