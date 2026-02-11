package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.PasswordResetToken;
import com.andrewsmith.financestracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime date);
    void deleteByUser(User user);
}
