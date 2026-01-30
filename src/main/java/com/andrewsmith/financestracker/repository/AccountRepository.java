package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Account;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    List<Account> findAllByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
