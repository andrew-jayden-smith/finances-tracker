package com.andrewsmith.financestracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    // @Value("${spring.mail.username}")
    // private String fromEmail;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;

        // Debug: Check if environment variables are loaded
        System.out.println("=== EMAIL CONFIG DEBUG ===");
        System.out.println("MAIL_USERNAME env: " + System.getenv("MAIL_USERNAME"));
        System.out.println("MAIL_PASSWORD set: " + (System.getenv("MAIL_PASSWORD") != null ? "Yes" : "No"));
        System.out.println("From email: " + fromEmail);
    }

    // Send an Email
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
