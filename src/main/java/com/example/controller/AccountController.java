package com.example.controller;

import com.example.dto.request.CreateAccountRequest;
import com.example.dto.request.TransferRequest;
import com.example.service.AccountService;
import com.example.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransferService transferService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest transferRequest) {
        transferService.moneyTransfer(transferRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAllAccounts(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountsInfo(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccountForCustomer(@RequestBody CreateAccountRequest createAccountRequest) {
        return ResponseEntity.ok(accountService.createAccount(createAccountRequest).toString());
    }
}
