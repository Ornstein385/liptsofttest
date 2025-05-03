package com.example.dto;

import com.example.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

//TODO(Возвращай модель счета полностью или хотя бы основные поля
// TODO(Добавь что ли для приличия у клиенто и счетов доп инфу)

public record AccountInfoResponse(UUID id, String currency, BigDecimal balance, String name) {

    public AccountInfoResponse(Account account) {
        this(account.getId(), account.getCurrency(), account.getBalance(), account.getName());
    }
}
