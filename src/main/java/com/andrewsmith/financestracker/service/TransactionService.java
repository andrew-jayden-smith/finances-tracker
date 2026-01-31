package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.Transaction;
import com.andrewsmith.financestracker.model.TransactionType;
import com.andrewsmith.financestracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    // List all transactions associated with the account
    public List<Transaction> getAllTransactionsByAccount(Account account) {
        return transactionRepository.findAllByAccount(account);
    }
    // Find transaction by id option
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    // Create a new Transaction
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    // Update a current transaction
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    // Delete a transaction
    public void deleteTransaction(Transaction transaction) {
        transactionRepository.delete(transaction);
    }
    // Filter to find transaction by date ranges
    public List<Transaction> getTransactionByAccountAndDate(Account account, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findAllByAccountAndDateBetween(account, start, end);
    }
    // Filter to find transaction by account and type
    public List<Transaction> getTransactionByAccountAndType(Account account, TransactionType type) {
        return transactionRepository.findAllByAccountAndType(account, type);
    }
    // Filter to find transaction by date ranges
    public List<Transaction> getTransactionByAccountIdAndType(Long accountId, TransactionType type) {
        return transactionRepository.findAllByAccountIdAndType(accountId, type);
    }
}
