package com.erenaskin.banking_dashboard.mapper;

import com.erenaskin.banking_dashboard.dto.RegisterRequest;
import com.erenaskin.banking_dashboard.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest registerRequest);
}
