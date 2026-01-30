package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.Transaction;
import com.andrewsmith.financestracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findAllByAccountIdAndType(Long accountId, TransactionType type);
    List<Transaction> findAllByAccountAndDateBetween(Account account, LocalDateTime start, LocalDateTime end);
    List<Transaction> findAllByAccountAndType(Account account, TransactionType type);
}
