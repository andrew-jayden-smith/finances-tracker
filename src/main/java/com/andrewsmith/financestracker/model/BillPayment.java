package com.andrewsmith.financestracker.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class BillPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Bill bill;

    private LocalDate paidDate;

    private Double amountPaid;

    @ManyToOne
    private Transaction transaction; // OPTIONAL auto-match

    public BillPayment() {}

    public BillPayment(Bill bill, LocalDate paidDate, Double amountPaid, Transaction transaction) {
        this.bill = bill;
        this.paidDate = paidDate;
        this.amountPaid = amountPaid;
        this.transaction = transaction;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
