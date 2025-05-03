package com.example.service;

import com.example.dto.AccountInfoResponse;
import com.example.dto.CreateAccountRequest;
import com.example.dto.CreateCustomerRequest;
import com.example.dto.MoneyTransferRequest;
import com.example.entity.Account;
import com.example.entity.Customer;
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
//TODO(AccountService) +
public class AccountService {
    //TODO (В компоненты подтягиваем зависимости через конструктор, ты сам это на одном из собесов подсвечивал) ?
    // RequiredArgsConstructor генерирует конструктор включающий только final поля. так что мой способ - через конструктор, но неявный
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * Создание нового клиента.
     * @return uuid нового созданного клиента.
     */
    public UUID createCustomer(CreateCustomerRequest createAccountRequest) {
        var customer = new Customer();
        customer.setName(createAccountRequest.name());
        return customerRepository.save(customer).getId();
    }

    /**
     * @param createAccountRequest содержит uuid клиента, валюту счета, изначальную сумму.
     * @return uuid нового созданного счета.
     */
    //TODO(Это странно возвращать id на создание объекта в данном случае, на мой взгляд лучше объект) ?
    // по идее, id это единственное, что неизвестно до создания объекта
    public UUID createAccount(CreateAccountRequest createAccountRequest) {
        UUID customerId = createAccountRequest.customerId();
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Клиент для добавления счета не существует");
        }

        var account = new Account();
        account.setName(createAccountRequest.name());
        account.setCurrency(createAccountRequest.currency());
        account.setBalance(Optional.ofNullable(createAccountRequest.balance())
                .filter(x -> x.compareTo(BigDecimal.ZERO) > -1)
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
    //TODO(Сюда можно ебнуть уровень изоляции RepeatableRead - для понтов) ???
    // с RepeatableRead не проходит тест MoneyTransferConcurrencyTest

    /**
     * TODO(Ебать)
     * 1) Тяжело читаемая логика ?
     * а как тогда упростить?
     * 2) Много повторяющегося кода +
     * разве что счет отправителя/получателя, а больше сокращать нечего
     * 3) Про блокировки потом подумаю, тяжело так читать
     */

    @Transactional
    public void moneyTransfer(MoneyTransferRequest moneyTransferRequest) {

        Account from = findAccountById(moneyTransferRequest.fromAccount(), "Не найден счет отправителя");
        Account to = findAccountById(moneyTransferRequest.toAccount(), "Не найден счет получателя");

        BigDecimal amount = Optional.ofNullable(moneyTransferRequest.amount())
                .filter(x -> x.compareTo(BigDecimal.ZERO) > -1)
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

        //TODO(Нет обработки ошибок от бд) ?+
        // я не знаю, как нормально это сделать. сейчас если у меня что-то пойдет по пизде, то транзакция откатится,
        // а пользователь получит 500. думаешь, этого мало?

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

    /**
     * @param customerId uuid клиента.
     * @return список данных о счетах клиента.
     */
    public List<AccountInfoResponse> getAccountsInfo(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Клиент с таким id не существует");
        }
        return accountRepository.findByCustomerId(customerId).stream().map(AccountInfoResponse::new).toList();
    }

    private final ConcurrentMap<UUID, ReentrantLock> lockRegistry = new ConcurrentHashMap<>();

    /**
     * Чтобы избежать дедлока — блокировать в порядке возрастания UUID
     */
    private void lockAccounts(UUID id1, UUID id2) {
        //TODO(комменты в маленьких методах - в виде доки) +

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
