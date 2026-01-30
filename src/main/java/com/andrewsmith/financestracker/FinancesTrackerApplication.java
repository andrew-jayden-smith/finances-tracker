package com.andrewsmith.financestracker;

import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FinancesTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancesTrackerApplication.class, args);
    }

    @Bean
    CommandLineRunner testUserRepo(UserRepository userRepository) {
        return args -> {
            User user = userRepository.findByUsername("smithdr3w");

            if (user != null) {
                System.out.println("Found User");
                System.out.println("ID: " + user.getId());
                System.out.println("Username: " + user.getUsername());
                System.out.println("Email: " + user.getEmail());
            } else {
                System.out.println("User not found");
            }
        };
    }
}
