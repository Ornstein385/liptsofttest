package com.example.controller;

import com.example.dto.CreateCustomerRequest;
import com.example.entity.Customer;
import com.example.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers")
//TODO(Я бы не выносил в отдельный /admin поинт, граммотней сделать контроллер только для счетов и только для клиентских данных) +
public class CustomerController {
    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(@RequestBody CreateCustomerRequest createCustomerRequest) {
        try {
            return ResponseEntity.ok(accountService.createCustomer(createCustomerRequest));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
