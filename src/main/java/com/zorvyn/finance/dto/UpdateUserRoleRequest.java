package com.zorvyn.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request body when an Admin changes a user's role. */
public class UpdateUserRoleRequest {

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "VIEWER|ANALYST|ADMIN", message = "Role must be VIEWER, ANALYST, or ADMIN")
    private String role;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
