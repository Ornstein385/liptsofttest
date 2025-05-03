package com.example.dto;

import java.math.BigDecimal;
import java.util.UUID;

// TODO: Насколько я знаю, в последних версиях Java есть record, которые работают как data class +
// TODO: CreateAccountRequest — это Request, то что получаем. DTO — почти всегда то, что отдаем. +
public record CreateAccountRequest(
        UUID customerId,        // TODO: Почему customerId не UUID +
        String currency,        // TODO: currency — ENUM!!!! ?
        BigDecimal balance,      // TODO: BigDecimal, и не важно, что DTO +
        String name
) {
}
