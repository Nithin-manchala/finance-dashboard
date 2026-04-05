package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.UpdateUserRoleRequest;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController — endpoints for managing users.
 * ALL endpoints require ADMIN role (enforced in UserService).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /** Get all users. ADMIN only. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<User> users = userService.getAllUsers(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    /** Get one user by ID. ADMIN only. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(
            @PathVariable Long id,
            HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        User user = userService.getUserById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User fetched", user));
    }

    /**
     * Update a user's role. Body: { "role": "ANALYST" }
     * Valid values: VIEWER, ANALYST, ADMIN
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest roleRequest,
            HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        userService.updateRole(id, roleRequest.getRole(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("User role updated to " + roleRequest.getRole()));
    }

    /**
     * Activate or deactivate a user. Body: { "status": "INACTIVE" }
     * ResponseEntity<?> wildcard avoids a compile-time type mismatch between
     * ApiResponse<Void> (success path) and ApiResponse<?> (error path).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String newStatus = body.get("status");
        if (newStatus == null
                || (!newStatus.equalsIgnoreCase("ACTIVE")
                &&  !newStatus.equalsIgnoreCase("INACTIVE"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status must be either ACTIVE or INACTIVE"));
        }

        User currentUser = (User) request.getAttribute("currentUser");
        userService.updateStatus(id, newStatus, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                "User status updated to " + newStatus.toUpperCase()));
    }
}
