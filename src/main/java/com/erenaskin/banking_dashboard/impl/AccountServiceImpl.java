package com.erenaskin.banking_dashboard.impl;

import com.erenaskin.banking_dashboard.dto.*;
import com.erenaskin.banking_dashboard.entity.*;
import com.erenaskin.banking_dashboard.mapper.*;
import com.erenaskin.banking_dashboard.repository.*;
import com.erenaskin.banking_dashboard.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public void createAccount(AccountRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = Account.builder()
                .iban(generateIban())
                .balance(BigDecimal.ZERO)
                .currency(Currency.valueOf(request.currency()))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        accountRepository.save(account);
    }

    @Override
    public List<AccountResponse> getUserAccounts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return accountRepository.findByUser(user).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public Currency getCurrencyByIban(String iban) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You don't have access to this account");
        }

        return account.getCurrency();
    }

    @Override
    public AccountDetailsResponse getAccountDetails(String iban) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You don't have access to this account");
        }

        List<Transaction> transactions = transactionRepository.findAllByAccount_IbanOrderByTimestampDesc(iban);

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();

        return AccountDetailsResponse.builder()
                .iban(account.getIban())
                .currency(account.getCurrency().toString())
                .balance(account.getBalance())
                .transactions(transactionResponses)
                .build();
    }

    @Override
    public void createTransaction(String iban, TransferRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Account senderAccount = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        if (!senderAccount.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You don't have access to this account");
        }

        BigDecimal amount = request.getAmount();
        TransactionType type = TransactionType.valueOf(request.getType());

        if (type == TransactionType.DEPOSIT) {
            senderAccount.setBalance(senderAccount.getBalance().add(amount));
            accountRepository.save(senderAccount);

            transactionRepository.save(Transaction.builder()
                    .type(type)
                    .receiverIban(iban)
                    .amount(amount)
                    .timestamp(LocalDateTime.now())
                    .account(senderAccount)
                    .build());

        } else if (type == TransactionType.WITHDRAW) {
            if (senderAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance");
            }
            senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
            accountRepository.save(senderAccount);

            transactionRepository.save(Transaction.builder()
                    .type(type)
                    .senderIban(iban)
                    .amount(amount)
                    .timestamp(LocalDateTime.now())
                    .account(senderAccount)
                    .build());

        } else if (type == TransactionType.TRANSFER) {
            if (request.getReceiverIban() == null || request.getReceiverIban().isBlank()) {
                throw new IllegalArgumentException("Receiver IBAN must be provided for transfer");
            }
            if (senderAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance");
            }
            Account receiverAccount = accountRepository.findByIban(request.getReceiverIban())
                    .orElseThrow(() -> new IllegalArgumentException("Receiver account not found: " + request.getReceiverIban()));

            senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
            receiverAccount.setBalance(receiverAccount.getBalance().add(amount));

            accountRepository.save(senderAccount);
            accountRepository.save(receiverAccount);

            transactionRepository.save(Transaction.builder()
                    .type(type)
                    .senderIban(iban)
                    .receiverIban(request.getReceiverIban())
                    .amount(amount)
                    .timestamp(LocalDateTime.now())
                    .account(senderAccount)
                    .build());

        } else {
            throw new IllegalArgumentException("Invalid transaction type");
        }
    }

    private String generateIban() {
        StringBuilder iban = new StringBuilder("TR");
        iban.append(randomDigits(2));
        iban.append("00061");
        iban.append(randomDigits(16));
        return iban.toString();
    }

    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}