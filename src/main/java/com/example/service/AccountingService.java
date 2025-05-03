package com.example.service;

import com.example.dto.AccountInfoDto;
import com.example.dto.CreateAccountDto;
import com.example.dto.MoneyTransferDto;
import com.example.entity.Account;
import com.example.entity.Customer;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AccountingService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * Создание нового клиента.
     * @return uuid нового созданного клиента.
     */
    public UUID createCustomer() {
        return customerRepository.save(new Customer()).getId();
    }

    /**
     *
     * @param createAccountDto содержит uuid клиента, валюту счета, изначальную сумму.
     * @return uuid нового созданного счета.
     */
    public UUID createAccount(CreateAccountDto createAccountDto) {
        UUID customerId = UUID.fromString(createAccountDto.getCustomerId());
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Клиент для добавления счета не существует");
        }

        var account = new Account();
        account.setCurrency(createAccountDto.getCurrency());
        account.setBalance(Optional.ofNullable(createAccountDto.getBalance())
                .map(BigDecimal::new).filter(x -> x.compareTo(BigDecimal.ZERO) > -1)
                .orElseThrow(() -> new IllegalArgumentException("Некорректное значение суммы")));
        var customer = new Customer();
        customer.setId(customerId);
        account.setCustomer(customer);
        return accountRepository.save(account).getId();
    }

    /**
     * Перевод средств между счетами клиента.
     * @param moneyTransferDto содержит uuid счета отправления, получения, сумму перевода.
     */
    @Transactional
    public void moneyTransfer(MoneyTransferDto moneyTransferDto) {
        Account from = Optional.ofNullable(moneyTransferDto.getFromAccount())
                .map(UUID::fromString).flatMap(accountRepository::findByIdForUpdate)
                .orElseThrow(() -> new IllegalArgumentException("Не найден счет отправителя"));
        Account to = Optional.ofNullable(moneyTransferDto.getToAccount())
                .map(UUID::fromString).flatMap(accountRepository::findByIdForUpdate)
                .orElseThrow(() -> new IllegalArgumentException("Не найден счет получателя"));
        BigDecimal amount = Optional.ofNullable(moneyTransferDto.getAmount())
                .map(BigDecimal::new).filter(x -> x.compareTo(BigDecimal.ZERO) > -1)
                .orElseThrow(() -> new IllegalArgumentException("Некорректное значение суммы"));
        if (from.getId().equals(to.getId())) {
            throw new IllegalStateException("Нельзя перевести со счета на этот же самый счет");
        }
        if (!from.getCustomer().getId().equals(to.getCustomer().getId())) {
            throw new IllegalStateException("Нельзя переводить с чужих счетов");
        }
        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new IllegalStateException("Нельзя переводить между счетами в разных валютах");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Нельзя уходить в минус по счету");
        }

        // Вариант, полагающийся на пессимистичную блокировку уровня БД

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

//        Вариант с блокировкой уровня Java-кода
//
//        lockAccounts(from.getId(), to.getId());
//        try {
//            from.setBalance(from.getBalance().subtract(amount));
//            to.setBalance(to.getBalance().add(amount));
//            accountRepository.save(from);
//            accountRepository.save(to);
//        } finally {
//            unlockAccounts(from.getId(), to.getId());
//        }

    }

    /**
     * @param customerId uuid клиента.
     * @return список данных о счетах клиента.
     */
    public List<AccountInfoDto> getAccountsInfo(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Клиент с таким id не существует");
        }
        return accountRepository.findByCustomerId(customerId).stream().map(AccountInfoDto::new).toList();
    }

    private final ConcurrentMap<UUID, ReentrantLock> lockRegistry = new ConcurrentHashMap<>();

    private void lockAccounts(UUID id1, UUID id2) {
        // чтобы избежать дедлока — блокировать в порядке возрастания UUID
        List<UUID> ordered = Stream.of(id1, id2).sorted().toList();
        lockRegistry.computeIfAbsent(ordered.get(0), k -> new ReentrantLock()).lock();
        lockRegistry.computeIfAbsent(ordered.get(1), k -> new ReentrantLock()).lock();
    }

    private void unlockAccounts(UUID id1, UUID id2) {
        // разблокирование в обратном порядке
        List<UUID> ordered = Stream.of(id1, id2).sorted().toList();
        lockRegistry.get(ordered.get(1)).unlock();
        lockRegistry.get(ordered.get(0)).unlock();

        lockRegistry.computeIfPresent(ordered.get(0), (k, v) -> v.hasQueuedThreads() ? v : null);
        lockRegistry.computeIfPresent(ordered.get(1), (k, v) -> v.hasQueuedThreads() ? v : null);
    }

}
