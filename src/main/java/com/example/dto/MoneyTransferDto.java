package com.example.dto;

import lombok.Data;

@Data
public class MoneyTransferDto {

    private String fromAccount;
    private String toAccount;
    private String amount;
}
