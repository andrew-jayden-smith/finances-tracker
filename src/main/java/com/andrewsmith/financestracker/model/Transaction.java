package com.andrewsmith.financestracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    // Database id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    // Owner
    @ManyToOne
    private Account account;

    // Transaction Data
    private BigDecimal amount;
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String description;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Merchant merchant;

    public Transaction() {}

    // Constructor to create a new transaction
    public Transaction(Account account, BigDecimal amount, TransactionType type, String description, LocalDateTime date, Category category, Merchant merchant) {
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = LocalDateTime.now();
        this.category = category;
        this.merchant = merchant;
    }

    // Constructor for Database
    public Transaction (Long id, Account account, BigDecimal amount, TransactionType type, String description, Category category, Merchant merchant) {
        this.account = account;
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = LocalDateTime.now();
        this.category = category;
        this.merchant = merchant;
    }

    // Methods for getters and setters
    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Merchant getMerchant() {
        return merchant;
    }

}






