package com.example;

import com.example.dto.MoneyTransferRequest;
import com.example.entity.Account;
import com.example.entity.Customer;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class MoneyTransferLogicTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account from;
    private Account to;

    @BeforeEach
    void setup() {
        Customer customer = customerRepository.save(new Customer());

        from = new Account();
        from.setCustomer(customer);
        from.setCurrency("RUB");
        from.setBalance(new BigDecimal("100"));

        to = new Account();
        to.setCustomer(customer);
        to.setCurrency("RUB");
        to.setBalance(new BigDecimal("100"));

        from = accountRepository.save(from);
        to = accountRepository.save(to);
    }

    /**
     * Перевод самому себе
     */
    @Test
    void testSameAccountTransferThrows() {
        MoneyTransferRequest dto = new MoneyTransferRequest(from.getId(), from.getId(), BigDecimal.ONE);

        assertThatThrownBy(() -> accountService.moneyTransfer(dto))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Перевод с чужого счета
     */
    @Test
    void testDifferentCustomersThrows() {
        Customer otherCustomer = customerRepository.save(new Customer());

        Account foreignAccount = new Account();
        foreignAccount.setCustomer(otherCustomer);
        foreignAccount.setCurrency("RUB");
        foreignAccount.setBalance(new BigDecimal("100"));
        foreignAccount = accountRepository.save(foreignAccount);

        MoneyTransferRequest dto = new MoneyTransferRequest(from.getId(), foreignAccount.getId(), BigDecimal.ONE);

        assertThatThrownBy(() -> accountService.moneyTransfer(dto))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Перевод на аккаунт в другой валюте
     */
    @Test
    void testDifferentCurrenciesThrows() {
        to.setCurrency("USD");
        accountRepository.save(to);

        MoneyTransferRequest dto = new MoneyTransferRequest(from.getId(), to.getId(), BigDecimal.ONE);

        assertThatThrownBy(() -> accountService.moneyTransfer(dto))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Недостаточно средств
     */
    @Test
    void testInsufficientFundsThrows() {
        MoneyTransferRequest dto = new MoneyTransferRequest(from.getId(), to.getId(), new BigDecimal("1000"));

        assertThatThrownBy(() -> accountService.moneyTransfer(dto))
                .isInstanceOf(IllegalStateException.class);
    }
}
