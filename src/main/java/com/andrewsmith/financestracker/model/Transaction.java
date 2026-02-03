package com.andrewsmith.financestracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    public Transaction() {}

    public Transaction(BigDecimal amount, TransactionType type,
                       String description, Category category, Merchant merchant) {

        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = LocalDateTime.now();
        this.category = category;
        this.merchant = merchant;
    }

    public Transaction(Long id, BigDecimal amount, TransactionType type,
                       String description, Category category, Merchant merchant) {

        this.id = id;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = LocalDateTime.now();
        this.category = category;
        this.merchant = merchant;
    }

    // Setters needed for Hibernate
    public void setUser(User user) { this.user = user; }
    public void setAccount(Account account) { this.account = account; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setType(TransactionType type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

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






