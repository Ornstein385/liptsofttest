package com.example.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID fromAccount,
        UUID toAccount,
        BigDecimal amount
) {
}
