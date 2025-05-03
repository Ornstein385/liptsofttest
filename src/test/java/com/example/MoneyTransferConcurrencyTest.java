package com.example;

import com.example.dto.MoneyTransferDto;
import com.example.entity.Account;
import com.example.entity.Customer;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.service.AccountingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class MoneyTransferConcurrencyTest {

    @Autowired
    private AccountingService accountingService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Выполнение множества одновременных переводов.
     */
    @Test
    void testConcurrentTransfers() throws InterruptedException {
        Customer customer = customerRepository.save(new Customer());

        Account from = new Account();
        from.setCustomer(customer);
        from.setCurrency("RUB");
        from.setBalance(new BigDecimal("10000"));

        Account to = new Account();
        to.setCustomer(customer);
        to.setCurrency("RUB");
        to.setBalance(BigDecimal.ZERO);

        from = accountRepository.save(from);
        to = accountRepository.save(to);

        UUID fromId = from.getId();
        UUID toId = to.getId();

        int transfers = 10000;
        CountDownLatch latch = new CountDownLatch(transfers);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, transfers).forEach(i ->
                    executor.submit(() -> {
                        try {
                            MoneyTransferDto dto = new MoneyTransferDto();
                            dto.setFromAccount(fromId.toString());
                            dto.setToAccount(toId.toString());
                            dto.setAmount("1");
                            accountingService.moneyTransfer(dto);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    })
            );

            latch.await(); // ждём завершения всех переводов
        }

        // Проверка балансов
        Account fromFinal = accountRepository.findById(fromId).orElseThrow();
        Account toFinal = accountRepository.findById(toId).orElseThrow();

        assertThat(fromFinal.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(toFinal.getBalance()).isEqualByComparingTo(new BigDecimal("10000"));
    }
}
