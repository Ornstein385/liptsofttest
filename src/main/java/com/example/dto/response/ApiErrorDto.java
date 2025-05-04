package com.example.dto.response;

public record ApiErrorDto(
        String description,
        String code,
        String exceptionName,
        String exceptionMessage) {
}
