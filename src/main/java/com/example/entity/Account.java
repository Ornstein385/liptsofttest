package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "account", indexes = {@Index(name = "idx_account_customer_id", columnList = "customer_id")})
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "balance", precision = 24, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
}
