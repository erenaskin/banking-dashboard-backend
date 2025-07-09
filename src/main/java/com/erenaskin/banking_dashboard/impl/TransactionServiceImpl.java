
package com.erenaskin.banking_dashboard.impl;

import com.erenaskin.banking_dashboard.dto.*;

import com.erenaskin.banking_dashboard.entity.*;
import com.erenaskin.banking_dashboard.mapper.*;
import com.erenaskin.banking_dashboard.repository.*;

import com.erenaskin.banking_dashboard.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public void transfer(TransferRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account sender = accountRepository.findByUser(user).stream()
                .filter(a -> a.getIban().equals(request.getSenderIban()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sender IBAN not found or not owned by user"));

        Account receiver = accountRepository.findAll().stream()
                .filter(a -> a.getIban().equals(request.getReceiverIban()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Receiver IBAN not found"));

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction tx = Transaction.builder()
                .senderIban(sender.getIban())
                .receiverIban(receiver.getIban())
                .amount(request.getAmount())
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
    }

    @Override
    public List<TransactionResponse> getHistory(String iban) {
        return transactionRepository.findBySenderIbanOrReceiverIban(iban, iban).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}
