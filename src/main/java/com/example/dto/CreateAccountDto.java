package com.example.dto;

import lombok.Data;

@Data
public class CreateAccountDto {

    private String customerId;
    private String currency;
    private String balance;
}
