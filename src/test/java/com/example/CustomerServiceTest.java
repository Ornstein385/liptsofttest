package com.example;

import com.example.dto.request.CreateCustomerRequest;
import com.example.entity.Customer;
import com.example.repository.CustomerRepository;
import com.example.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testCreateCustomerSuccess() {
        CreateCustomerRequest request = new CreateCustomerRequest("Иван Иванов");

        var customerId = customerService.createCustomer(request);

        assertThat(customerRepository.existsById(customerId)).isTrue();

        Customer customer = customerRepository.findById(customerId).orElseThrow();
        assertThat(customer.getName()).isEqualTo("Иван Иванов");
    }
}
