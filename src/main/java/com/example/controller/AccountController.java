package com.example.controller;

import com.example.dto.MoneyTransferDto;
import com.example.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountingService accountingService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody MoneyTransferDto moneyTransferDto) {
        try {
            accountingService.moneyTransfer(moneyTransferDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAllAccounts(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(accountingService.getAccountsInfo(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
