package com.example.controller;

import com.example.dto.CreateAccountRequest;
import com.example.dto.MoneyTransferRequest;
import com.example.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody MoneyTransferRequest moneyTransferRequest) {
        try {
            accountService.moneyTransfer(moneyTransferRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAllAccounts(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(accountService.getAccountsInfo(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccountForCustomer(@RequestBody CreateAccountRequest createAccountRequest) {
        try {
            return ResponseEntity.ok(accountService.createAccount(createAccountRequest).toString());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
