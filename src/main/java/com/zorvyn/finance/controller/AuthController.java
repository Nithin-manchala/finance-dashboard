package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.LoginRequest;
import com.zorvyn.finance.dto.RegisterRequest;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController — handles authentication endpoints.
 *
 * These endpoints are PUBLIC (no token required):
 *   POST /api/auth/register  → Create a new account
 *   POST /api/auth/login     → Get a token
 *
 * This endpoint REQUIRES a token:
 *   POST /api/auth/logout    → Invalidate your token
 *
 * @RestController = @Controller + @ResponseBody
 *   Means: "This class handles HTTP requests and returns JSON responses"
 *
 * @RequestMapping sets the base URL path for all methods in this class.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user.
     *
     * HTTP Method: POST
     * URL: /api/auth/register
     * Body: { "name": "...", "email": "...", "password": "..." }
     *
     * @Valid triggers the validation annotations on RegisterRequest.
     * If validation fails, GlobalExceptionHandler returns a 400 error.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        // Return user info (WITHOUT the password!) in the response
        Map<String, Object> data = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(ApiResponse.success("Registration successful! You can now log in.", data));
    }

    /**
     * Login and get a token.
     *
     * HTTP Method: POST
     * URL: /api/auth/login
     * Body: { "email": "...", "password": "..." }
     *
     * Returns a token. Include it in all subsequent requests:
     *   Authorization: Bearer <token>
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request) {

        Map<String, Object> loginData = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", loginData));
    }

    /**
     * Logout and invalidate the current token.
     *
     * HTTP Method: POST
     * URL: /api/auth/logout
     * Header: Authorization: Bearer <token>
     *
     * After this, the token can no longer be used.
     * Our AuthFilter reads the current user from request attributes (set in AuthFilter).
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {

        // Get the token from the Authorization header
        // (We know it's valid here because AuthFilter already verified it)
        String token = request.getHeader("Authorization").substring(7);
        authService.logout(token);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Get current user's profile.
     *
     * HTTP Method: GET
     * URL: /api/auth/me
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            HttpServletRequest request) {

        // AuthFilter attached the User object to the request under "currentUser"
        User user = (User) request.getAttribute("currentUser");

        Map<String, Object> data = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "status", user.getStatus()
        );

        return ResponseEntity.ok(ApiResponse.success("Profile fetched", data));
    }
}
