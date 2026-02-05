package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.*;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    //List<Transaction> findAllByAccountAndDateBetween(Account account, LocalDateTime start, LocalDateTime end);
    List<Transaction> findAllByAccountAndType(Account account, TransactionType type);
    List<Transaction> findAllByAccountIdAndType(Long accountId, TransactionType type);
    List<Transaction> findAllByAccount(Account account);
    List<Transaction> findAllByAccountAndCategory(Account account, Category category);
    List<Transaction> findAllByAccountAndMerchant(Account account, Merchant merchant);
    List<Transaction> findByAccountAndCategoryAndDateBetweenOrderByDateAsc(Account account, Category category, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByAccountAndMerchantAndDateBetweenOrderByDateAsc(Account account, Merchant merchant, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByAccountAndDateBetweenOrderByDateAsc(Account account, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByAccountAndDateBetweenOrderByDateDesc(Account account, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByAccountAndCategoryAndDateBetweenOrderByDateDesc(Account account, Category category, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByAccountAndMerchantAndDateBetweenOrderByDateDesc(Account account, Merchant merchant, LocalDateTime start, LocalDateTime end);

}
