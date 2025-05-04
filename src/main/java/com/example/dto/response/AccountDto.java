package com.example.dto.response;

import com.example.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(
        UUID id,
        String currency,
        BigDecimal balance,
        String name) {

    public AccountDto(Account account) {
        this(account.getId(), account.getCurrency(), account.getBalance(), account.getName());
    }
}
