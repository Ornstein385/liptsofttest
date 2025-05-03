package com.example.dto;

import com.example.entity.Account;
import lombok.Data;

@Data
public class AccountInfoDto {

    private String id;
    private String currency;
    private String balance;

    public AccountInfoDto(Account account) {
        this.id = account.getId().toString();
        this.currency = account.getCurrency();
        this.balance = account.getBalance().toString();
    }
}
