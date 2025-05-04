package com.example.controller;

import com.example.dto.response.ApiErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDto> handleIllegalArgument(IllegalArgumentException e) {
        return buildErrorResponse(e, "Некорректный аргумент", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorDto> handleIllegalState(IllegalStateException e) {
        return buildErrorResponse(e, "Неверное состояние", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneral(Exception e) {
        return buildErrorResponse(e, "Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorDto> buildErrorResponse(Exception e, String description, HttpStatus status) {
        ApiErrorDto error = new ApiErrorDto(description, status.toString(), e.getClass().getSimpleName(), e.getMessage());
        return new ResponseEntity<>(error, status);
    }
}

