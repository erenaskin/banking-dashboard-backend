// AccountService.java (interface)
package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.*;
import com.erenaskin.banking_dashboard.entity.Currency;

import java.util.List;

public interface AccountService {
    void createAccount(AccountRequest request);
    List<AccountResponse> getUserAccounts();
    Currency getCurrencyByIban(String iban);
    AccountDetailsResponse getAccountDetails(String iban);
    void createTransaction(String iban, TransferRequest request);
}