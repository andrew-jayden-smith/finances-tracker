package com.andrewsmith.financestracker.model;

import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private Long id;
    private String name;
    private BigDecimal openingBalance;
    private LocalDateTime openingDate;
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
