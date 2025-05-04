package com.example.service;

import com.example.dto.request.CreateAccountRequest;
import com.example.dto.response.AccountDto;
import com.example.entity.Account;
import com.example.entity.Customer;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * @param createAccountRequest содержит uuid клиента, валюту счета, изначальную сумму.
     * @return uuid нового созданного счета.
     */
    public UUID createAccount(CreateAccountRequest createAccountRequest) {
        UUID customerId = createAccountRequest.customerId();
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Клиент для добавления счета не существует");
        }

        var customer = new Customer();
        customer.setId(customerId);
        var account = new Account();
        account.setName(createAccountRequest.name());
        account.setCurrency(createAccountRequest.currency().name());
        account.setBalance(Optional.ofNullable(createAccountRequest.balance())
                .filter(x -> x.compareTo(BigDecimal.ZERO) > -1)
                .orElseThrow(() -> new IllegalArgumentException("Некорректное значение суммы")));

        account.setCustomer(customer);
        return accountRepository.save(account).getId();
    }

    /**
     * @param customerId uuid клиента.
     * @return список данных о счетах клиента.
     */
    public List<AccountDto> getAccountsInfo(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Клиент с таким id не существует");
        }
        return accountRepository.findByCustomerId(customerId).stream().map(AccountDto::new).toList();
    }

}
