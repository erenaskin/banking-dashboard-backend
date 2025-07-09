package com.erenaskin.banking_dashboard.mapper;

import com.erenaskin.banking_dashboard.dto.TransactionResponse;
import com.erenaskin.banking_dashboard.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toResponse(Transaction transaction);
}