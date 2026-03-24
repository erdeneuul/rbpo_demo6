package com.example.demo.security;

import com.example.demo.model.AppUser;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a default admin user on first startup if no users exist.
 * Username: admin
 * Password: Admin123!
 *
 * IMPORTANT: Change this password after first login!
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only create if no users exist yet
        if (userRepository.count() == 0) {
            // Create default admin
            AppUser admin = new AppUser(
                    "admin",
                    passwordEncoder.encode("Admin123!"),
                    "ROLE_ADMIN"
            );
            userRepository.save(admin);

            // Create default regular user
            AppUser user = new AppUser(
                    "user",
                    passwordEncoder.encode("User123!"),
                    "ROLE_USER"
            );
            userRepository.save(user);

            System.out.println("=== Default users created ===");
            System.out.println("Admin - username: admin, password: Admin123!");
            System.out.println("User  - username: user,  password: User123!");
            System.out.println("==============================");
        }
    }
}
