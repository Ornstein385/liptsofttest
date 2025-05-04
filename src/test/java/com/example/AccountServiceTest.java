package com.example;

import com.example.dto.request.CreateAccountRequest;
import com.example.dto.request.Currency;
import com.example.entity.Account;
import com.example.entity.Customer;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testCreateAccountSuccess() {
        Customer customer = new Customer();
        customer = customerRepository.save(customer);

        CreateAccountRequest request = new CreateAccountRequest(
                customer.getId(),
                Currency.RUB,
                new BigDecimal("500"),
                "My Account"
        );

        UUID accountId = accountService.createAccount(request);

        assertThat(accountRepository.existsById(accountId)).isTrue();

        Account account = accountRepository.findById(accountId).orElseThrow();
        assertThat(account.getName()).isEqualTo("My Account");
        assertThat(account.getBalance()).isEqualByComparingTo("500");
        assertThat(account.getCurrency()).isEqualTo("RUB");
    }

    @Test
    void testCreateAccountWithNonExistingCustomerThrows() {
        UUID invalidCustomerId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
                invalidCustomerId,
                Currency.RUB,
                BigDecimal.TEN,
                "Invalid Account"
        );

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Клиент для добавления счета не существует");
    }

    @Test
    void testCreateAccountWithNegativeBalanceThrows() {
        Customer customer = customerRepository.save(new Customer());

        CreateAccountRequest request = new CreateAccountRequest(
                customer.getId(),
                Currency.RUB,
                new BigDecimal("-100"),
                "Negative Account"
        );

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Некорректное значение суммы");
    }

    @Test
    void testGetAccountsInfoSuccess() {
        Customer customer = customerRepository.save(new Customer());

        Account account1 = new Account();
        account1.setCustomer(customer);
        account1.setBalance(new BigDecimal("100"));
        account1.setCurrency("RUB");
        account1.setName("Account 1");
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setCustomer(customer);
        account2.setBalance(new BigDecimal("200"));
        account2.setCurrency("RUB");
        account2.setName("Account 2");
        accountRepository.save(account2);

        var accounts = accountService.getAccountsInfo(customer.getId());

        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting("name").contains("Account 1", "Account 2");
    }

    @Test
    void testGetAccountsInfoForNonExistentCustomerThrows() {
        UUID fakeId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.getAccountsInfo(fakeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Клиент с таким id не существует");
    }
}
