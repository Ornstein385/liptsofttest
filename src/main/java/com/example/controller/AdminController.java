package com.example.controller;

import com.example.dto.CreateAccountDto;
import com.example.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AccountingService accountingService;

    @PostMapping("/create-customer")
    public ResponseEntity<?> createCustomer() {
        try {
            return ResponseEntity.ok(accountingService.createCustomer().toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-account")
    public ResponseEntity<?> createAccountForCustomer(@RequestBody CreateAccountDto createAccountDto) {
        try {
            return ResponseEntity.ok(accountingService.createAccount(createAccountDto).toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
