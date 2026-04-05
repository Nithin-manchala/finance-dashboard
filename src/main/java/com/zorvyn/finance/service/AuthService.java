package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.LoginRequest;
import com.zorvyn.finance.dto.RegisterRequest;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AuthService handles authentication logic: register, login, logout.
 *
 * This is the SERVICE layer — it contains BUSINESS LOGIC.
 * The controller receives the HTTP request, passes it here,
 * and this class decides what to do with it.
 *
 * Layered architecture:
 *   Controller → Service → Repository → Database
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * BCryptPasswordEncoder is used to:
     *   1. Hash passwords before saving (encode)
     *   2. Verify passwords during login (matches)
     *
     * BCrypt is a one-way hash — you can NEVER reverse it to get the original password.
     * Even the database admin can't see real passwords.
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Register a new user.
     * - Validates that email is not already taken
     * - Hashes the password with BCrypt
     * - Saves the user with VIEWER role (default for all new signups)
     */
    public User register(RegisterRequest request) {
        // Check if email is already registered
        if (userRepository.emailExists(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        // Create the new user object
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        // IMPORTANT: Hash the password before saving — NEVER store plain text!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("VIEWER");   // New users start as VIEWER (Admin can promote later)
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    /**
     * Login a user.
     * - Finds the user by email
     * - Verifies the password using BCrypt
     * - Creates a session token and returns it
     *
     * Returns a Map containing the token and user info.
     * The client must send this token in every subsequent request.
     */
    public Map<String, Object> login(LoginRequest request) {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().toLowerCase().trim());

        if (userOpt.isEmpty()) {
            // Use the same message for "user not found" and "wrong password"
            // to prevent attackers from knowing which emails exist
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Check if account is active
        if (!user.isActive()) {
            throw new IllegalArgumentException("Your account has been deactivated. Contact an administrator.");
        }

        // Verify password: BCrypt compares raw password against the stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generate and store a token (replaces old one if exists)
        String token = userRepository.createToken(user.getId());

        // Build the response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("message", "Login successful. Use the token in the Authorization header.");

        return response;
    }

    /**
     * Logout: delete the token from the database.
     * After this, any request using this token will be rejected by our AuthFilter.
     */
    public void logout(String token) {
        userRepository.deleteToken(token);
    }

    /**
     * Creates a default admin user if no users exist in the database.
     * Called by DataInitializer on application startup.
     */
    public void createDefaultAdminIfNeeded() {
        if (!userRepository.emailExists("admin@finance.com")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@finance.com");
            admin.setPassword(passwordEncoder.encode("admin123"));  // Change after first login!
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            userRepository.save(admin);
            System.out.println("✅ Default admin created: admin@finance.com / admin123");
        }
    }
}
