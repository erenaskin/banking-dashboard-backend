package com.erenaskin.banking_dashboard.repository;

import com.erenaskin.banking_dashboard.entity.Account;
import com.erenaskin.banking_dashboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByIban(String iban);
}
