package com.zorvyn.finance.service;

import com.zorvyn.finance.exception.AccessDeniedException;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserService handles business logic for user management operations.
 * Only Admins can perform these operations — access control is checked here.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /** Get all users. Admin only. */
    public List<User> getAllUsers(User currentUser) {
        requireAdmin(currentUser);
        return userRepository.findAll();
    }

    /** Get a user by ID. Admin only. */
    public User getUserById(Long id, User currentUser) {
        requireAdmin(currentUser);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    /** Update a user's role. Admin only. Cannot change your own role. */
    public void updateRole(Long userId, String newRole, User currentUser) {
        requireAdmin(currentUser);

        // Prevent admin from accidentally removing their own admin access
        if (currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        // Verify the target user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        userRepository.updateRole(userId, newRole.toUpperCase());
    }

    /** Update a user's status (ACTIVE/INACTIVE). Admin only. */
    public void updateStatus(Long userId, String newStatus, User currentUser) {
        requireAdmin(currentUser);

        if (currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        userRepository.updateStatus(userId, newStatus.toUpperCase());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Throws AccessDeniedException if the current user is not an ADMIN. */
    private void requireAdmin(User user) {
        if (!user.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only administrators can manage users");
        }
    }
}
