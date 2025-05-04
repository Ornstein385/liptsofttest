package com.example.service;

import com.example.dto.request.TransferRequest;
import com.example.entity.Account;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class TransferService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * Перевод средств между счетами клиента.
     *
     * @param transferRequest содержит uuid счета отправления, получения, сумму перевода.
     */

    @Transactional
    public void moneyTransfer(TransferRequest transferRequest) {

        Account from = findAccountById(transferRequest.fromAccount(), "Не найден счет отправителя");
        Account to = findAccountById(transferRequest.toAccount(), "Не найден счет получателя");

        BigDecimal amount = Optional.ofNullable(transferRequest.amount())
                .filter(x -> x.compareTo(BigDecimal.ZERO) > 0)
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

    private Account findAccountById(UUID id, String errorMessage) {
        return Optional.ofNullable(id).flatMap(accountRepository::findByIdForUpdate)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    private final ConcurrentMap<UUID, ReentrantLock> lockRegistry = new ConcurrentHashMap<>();

    /**
     * Чтобы избежать дедлока — блокировать в порядке возрастания UUID
     */
    private void lockAccounts(UUID id1, UUID id2) {
        List<UUID> ordered = Stream.of(id1, id2).sorted().toList();
        lockRegistry.computeIfAbsent(ordered.get(0), k -> new ReentrantLock()).lock();
        lockRegistry.computeIfAbsent(ordered.get(1), k -> new ReentrantLock()).lock();
    }

    /**
     * Разблокирование в обратном порядке
     */
    private void unlockAccounts(UUID id1, UUID id2) {
        List<UUID> ordered = Stream.of(id1, id2).sorted().toList();
        lockRegistry.get(ordered.get(1)).unlock();
        lockRegistry.get(ordered.get(0)).unlock();

        lockRegistry.computeIfPresent(ordered.get(0), (k, v) -> v.hasQueuedThreads() ? v : null);
        lockRegistry.computeIfPresent(ordered.get(1), (k, v) -> v.hasQueuedThreads() ? v : null);
    }
}
