package com.zorvyn.finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO = Data Transfer Object
 *
 * This class holds the data we expect in the request body when a user logs in.
 * We use @NotBlank and @Email as validation annotations — Spring will
 * automatically reject requests that don't match these rules.
 *
 * The @Valid annotation in the controller triggers this validation.
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
