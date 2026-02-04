package com.andrewsmith.financestracker.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    private String name; // Electric, Gas, Internet

    private BigDecimal amount; // Payment amount

    @Enumerated(EnumType.STRING)
    private BillStatus status; // Pending, Paid

    @Enumerated(EnumType.STRING)
    private BillFrequency frequency; // Annual, Monthly, ...

    private int dueDay;
    private int billingMonth; // 1-12
    private int billingYear; // 2026 - Other bills that do not occur monthly

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // JPA
    public Bill() {}

    // Constructor for new expense
    public Bill(User user, String name, BigDecimal amount, int dueDay, int billingMonth, int billingYear) {
        this.user = user;
        this.name = name;
        this.amount = amount;
        this.dueDay = dueDay;
        this.billingMonth = billingMonth;
        this.billingYear = billingYear;
        this.status = BillStatus.DUE;
        this.frequency = null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BillStatus getStatus() {
        return status;
    }
    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public BillFrequency getFrequency() {
        return frequency;
    }
    public void setFrequency(BillFrequency frequency) {
        this.frequency = frequency;
    }

    public int getDueDay() {
        return dueDay;
    }
    public void setDueDay(int dueDay) {
        this.dueDay = dueDay;
    }

    public int getBillingMonth() {
        return billingMonth;
    }
    public void setBillingMonth(int billingMonth) {
        this.billingMonth = billingMonth;
    }

    public int getBillingYear() {
        return billingYear;
    }
    public void setBillingYear(int billingYear) {
        this.billingYear = billingYear;
    }

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}
