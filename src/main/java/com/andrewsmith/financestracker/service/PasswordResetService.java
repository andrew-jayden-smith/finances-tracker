package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.PasswordResetToken;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiry-minutes:60}")
    private int tokenExpiryMinutes;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordResetService(EmailService emailService, PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    // Generate random tokens
    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Create and save password reset w/ token
    @Transactional
    public PasswordResetToken createPasswordResetToken(User user) {
        // Delete any existing tokens for user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Generate new token
        String token = generateToken();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, tokenExpiryMinutes);

        return tokenRepository.save(resetToken);
    }

    // Send reset email to user
    public void sendPasswordResetEmail(User user, PasswordResetToken token) {
        String resetUrl = baseUrl + "/user/reset-password?token=" + token.getToken();

        String subject = "Franklin FinTrack - Password Reset Request";
        String message = String.format(
                "Hello %s, \n\n" +
                "You requested to reset your password. Click the link below to reset it:\n\n" +
                "%s\n\n" +
                "This link will expire in %d minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Franklin FinTrack Team",
                user.getUsername(), resetUrl, tokenExpiryMinutes
        );
        emailService.sendEmail(user.getEmail(), subject, message);
    }

    // Validate password reset token
    public Optional<PasswordResetToken> validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isPresent()) {
            PasswordResetToken t = resetToken.get();
            if (t.isValid()) {
                return resetToken;
            }
        }
        return Optional.empty();
    }

    // Mark token used
    @Transactional
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }

    // Delete token after reset
    @Transactional
    public void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }

    // Clean expired tokens
    public void deleteExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
