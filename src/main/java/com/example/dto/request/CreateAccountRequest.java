package com.example.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountRequest(
        UUID customerId,
        Currency currency,
        BigDecimal balance,
        String name
) {
}
