package com.andrewsmith.financestracker.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private BigDecimal openingBalance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime openingDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "display_order")
    private Integer displayOrder;

    public Account() {}

    public Account(String name, BigDecimal openingBalance, LocalDateTime openingDate, User user) {
        this.name = name;
        this.openingBalance = openingBalance;
        this.user = user;
        this.openingDate = openingDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}

