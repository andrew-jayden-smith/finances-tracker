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
    @JoinColumn(name = "user_id", nullable = false)
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public LocalDateTime getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDateTime openingDate) {
        this.openingDate = openingDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

