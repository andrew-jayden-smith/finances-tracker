package com.andrewsmith.financestracker.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal openingBalance;

    @Column(nullable = false)
    private LocalDateTime openingDate;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;


    public Account() {}

    public Account(String name, BigDecimal openingBalance, LocalDateTime openingDate, User user) {
        this.name = name;
        this.openingBalance = openingBalance;
        this.openingDate = openingDate;
        this.user = user;
    }

    public Account(Long id, String name, BigDecimal openingBalance, LocalDateTime openingDate, User user) {
        this.id = id;
        this.name = name;
        this.openingBalance = openingBalance;
        this.openingDate = openingDate;
        this.user = user;
    }
}
