package com.example.dto;

import java.math.BigDecimal;
import java.util.UUID;

// TODO: Transaction — возможно, название стоит уточнить
public record MoneyTransferRequest(
        UUID fromAccount,       // TODO: Это что такое?
        UUID toAccount,         // TODO: Возвращай целиком Account — (если нужно больше данных, можно расширить)
        BigDecimal amount       // TODO: BigDecimal
) {
}
