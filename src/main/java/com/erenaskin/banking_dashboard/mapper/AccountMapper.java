package com.erenaskin.banking_dashboard.mapper;

import com.erenaskin.banking_dashboard.dto.AccountResponse;
import com.erenaskin.banking_dashboard.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountResponse toResponse(Account account);
}