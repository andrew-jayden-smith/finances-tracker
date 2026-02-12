package com.andrewsmith.financestracker.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Track reset attempts per email
    private final Map<String, AttemptInfo> resetAttempts = new ConcurrentHashMap<>();

    // Track reset attempts per IP
    private final Map<String, AttemptInfo> ipAttempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS_PER_EMAIL = 3;  // 3 attempts per email
    private static final int MAX_ATTEMPTS_PER_IP = 5;      // 5 attempts per IP
    private static final int LOCKOUT_MINUTES = 15;         // 15 minute lockout

    // Check if email can request password reset
    public boolean canRequestReset(String email, String ipAddress) {
        // Clean up old attempts
        cleanupExpiredAttempts();

        // Check email rate limit
        if (!canAttempt(resetAttempts, email.toLowerCase(), MAX_ATTEMPTS_PER_EMAIL)) {
            return false;
        }

        // Check IP rate limit
        if (!canAttempt(ipAttempts, ipAddress, MAX_ATTEMPTS_PER_IP)) {
            return false;
        }

        return true;
    }

    // Record a password reset attempt
    public void recordAttempt(String email, String ipAddress) {
        String emailKey = email.toLowerCase();

        // Record email attempt
        resetAttempts.compute(emailKey, (k, v) -> {
            if (v == null) {
                return new AttemptInfo(1, LocalDateTime.now());
            } else {
                v.incrementCount();
                return v;
            }
        });

        // Record IP attempt
        ipAttempts.compute(ipAddress, (k, v) -> {
            if (v == null) {
                return new AttemptInfo(1, LocalDateTime.now());
            } else {
                v.incrementCount();
                return v;
            }
        });
    }

    // Get minutes until email can try again
    public int getMinutesUntilRetry(String email) {
        String emailKey = email.toLowerCase();
        AttemptInfo info = resetAttempts.get(emailKey);

        if (info == null || info.count < MAX_ATTEMPTS_PER_EMAIL) {
            return 0;
        }

        LocalDateTime unlockTime = info.firstAttempt.plusMinutes(LOCKOUT_MINUTES);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(unlockTime)) {
            return (int) java.time.Duration.between(now, unlockTime).toMinutes() + 1;
        }

        return 0;
    }

    // Check if key can make another attempt
    private boolean canAttempt(Map<String, AttemptInfo> attempts, String key, int maxAttempts) {
        AttemptInfo info = attempts.get(key);

        if (info == null) {
            return true;
        }

        // Check if lockout period has expired
        LocalDateTime unlockTime = info.firstAttempt.plusMinutes(LOCKOUT_MINUTES);
        if (LocalDateTime.now().isAfter(unlockTime)) {
            attempts.remove(key);  // Reset the attempts
            return true;
        }

        // Check if under limit
        return info.count < maxAttempts;
    }

    // Clean up attempts older than lockout period
    private void cleanupExpiredAttempts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);

        resetAttempts.entrySet().removeIf(entry ->
                entry.getValue().firstAttempt.isBefore(cutoff)
        );

        ipAttempts.entrySet().removeIf(entry ->
                entry.getValue().firstAttempt.isBefore(cutoff)
        );
    }

    // Inner class to track attempt info
    private static class AttemptInfo {
        int count;
        LocalDateTime firstAttempt;

        AttemptInfo(int count, LocalDateTime firstAttempt) {
            this.count = count;
            this.firstAttempt = firstAttempt;
        }

        void incrementCount() {
            this.count++;
        }
    }
}