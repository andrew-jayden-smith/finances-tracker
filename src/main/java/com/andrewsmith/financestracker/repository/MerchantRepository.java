package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Merchant;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;

import java.util.List;

public interface MerchantRepository extends CrudRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {
    Merchant findByName(String name);
    boolean existsByName(String name);
    List<Merchant> findByNameContainingIgnoreCase(String name);
}
