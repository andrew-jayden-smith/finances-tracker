package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    List<Account> findAllByUser(User user);
    boolean existsByUserAndName(User user, String name);
}
