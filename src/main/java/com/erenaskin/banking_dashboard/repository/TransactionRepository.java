package com.erenaskin.banking_dashboard.repository;

import com.erenaskin.banking_dashboard.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderIbanOrReceiverIban(String senderIban, String receiverIban);
    List<Transaction> findAllByAccount_IbanOrderByTimestampDesc(String iban);

}
