package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.*;
import com.andrewsmith.financestracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
        return transactionRepository.findByAccountAndDateBetweenOrderByDateAsc(account, start, end);
    }
    // Filter to find transaction by account and type
    public List<Transaction> getTransactionByAccountAndType(Account account, TransactionType type) {
        return transactionRepository.findAllByAccountAndType(account, type);
    }

    // Filter to find transaction by date ranges
    public List<Transaction> getTransactionByAccountIdAndType(Long accountId, TransactionType type) {
        return transactionRepository.findAllByAccountIdAndType(accountId, type);
    }

    public List<Transaction> getTransactionsByAccountAndCategory(Account account, Category category) {
        return transactionRepository.findAllByAccountAndCategory(account, category);
    }

    public List<Transaction> getTransactionsByAccountAndMerchant(Account account, Merchant merchant) {
        return transactionRepository.findAllByAccountAndMerchant(account, merchant);
    }


    public List<Transaction> getTransactionsByAccountAndCategoryAndDateAsc(Account account, Category category, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountAndCategoryAndDateBetweenOrderByDateAsc(account, category, start, end);
    }

    public List<Transaction> getTransactionsByAccountAndMerchantAndDateAsc(Account account, Merchant merchant, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountAndMerchantAndDateBetweenOrderByDateAsc(account, merchant, start, end);
    }

    public List<Transaction> getTransactionByAccountAndDateDesc(Account account, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountAndDateBetweenOrderByDateDesc(account, start, end);
    }

    public List<Transaction> getTransactionsByAccountAndCategoryAndDateDesc(Account account, Category category, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountAndCategoryAndDateBetweenOrderByDateDesc(account, category, start, end);
    }

    public List<Transaction> getTransactionsByAccountAndMerchantAndDateDesc(Account account, Merchant merchant, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountAndMerchantAndDateBetweenOrderByDateDesc(account, merchant, start, end);
    }

    // ============================================
// ADD/REPLACE THESE METHODS IN TransactionService.java
// ============================================

    /**
     * Get comprehensive spending summary with income and expenses separated
     * Returns a map containing:
     * - "expenses": Map<String, BigDecimal> of expense categories
     * - "income": Map<String, BigDecimal> of income categories
     * - "totalExpenses": BigDecimal
     * - "totalIncome": BigDecimal
     */
    public Map<String, Object> getComprehensiveSpendingSummary(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByAccountAndDateBetweenOrderByDateDesc(
                    account, startDate, endDate
            );
        } else {
            transactions = transactionRepository.findAllByAccount(account);
        }

        Map<String, BigDecimal> expenseCategories = new HashMap<>();
        Map<String, BigDecimal> incomeCategories = new HashMap<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = (t.getCategory() != null)
                    ? t.getCategory().getName()
                    : "Uncategorized";

            if (t.getAmount().signum() < 0) {
                // Expense (negative amount)
                BigDecimal absAmount = t.getAmount().abs();
                BigDecimal currentTotal = expenseCategories.getOrDefault(categoryName, BigDecimal.ZERO);
                expenseCategories.put(categoryName, currentTotal.add(absAmount));
                totalExpenses = totalExpenses.add(absAmount);
            } else if (t.getAmount().signum() > 0) {
                // Income (positive amount)
                BigDecimal currentTotal = incomeCategories.getOrDefault(categoryName, BigDecimal.ZERO);
                incomeCategories.put(categoryName, currentTotal.add(t.getAmount()));
                totalIncome = totalIncome.add(t.getAmount());
            }
            // Ignore zero amounts
        }

        // Sort expenses by amount (descending)
        List<Map.Entry<String, BigDecimal>> sortedExpenses = new ArrayList<>(expenseCategories.entrySet());
        sortedExpenses.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("expensesSorted", sortedExpenses);
        result.put("expenses", expenseCategories);
        result.put("income", incomeCategories);
        result.put("totalExpenses", totalExpenses);
        result.put("totalIncome", totalIncome);

        return result;
    }

    /**
     * Get category summary for expenses only (for backwards compatibility)
     */
    public List<Map.Entry<String, BigDecimal>> getSortedCategorySummary(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = getComprehensiveSpendingSummary(account, startDate, endDate);
        return (List<Map.Entry<String, BigDecimal>>) summary.get("expensesSorted");
    }

}
