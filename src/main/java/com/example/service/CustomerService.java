package com.example.service;

import com.example.dto.request.CreateCustomerRequest;
import com.example.entity.Customer;
import com.example.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Создание нового клиента.
     *
     * @return uuid нового созданного клиента.
     */
    public UUID createCustomer(CreateCustomerRequest createAccountRequest) {
        var customer = new Customer();
        customer.setName(createAccountRequest.name());
        return customerRepository.save(customer).getId();
    }
}
