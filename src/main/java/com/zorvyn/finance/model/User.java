package com.zorvyn.finance.model;

/**
 * Represents a User in our system.
 * This is a plain Java class (POJO) that maps to the 'users' table in MySQL.
 *
 * Role hierarchy:
 *   VIEWER  → Can only read/view data
 *   ANALYST → Can read + access insights/summaries
 *   ADMIN   → Full access: create, update, delete records and manage users
 */
public class User {

    private Long id;
    private String name;
    private String email;
    private String password;  // Always stored as BCrypt hash
    private String role;      // "VIEWER", "ANALYST", or "ADMIN"
    private String status;    // "ACTIVE" or "INACTIVE"
    private String createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String name, String email, String password, String role, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ── Helper Methods ────────────────────────────────────────────────────────

    /** Checks if this user has at least one of the given roles. */
    public boolean hasRole(String... roles) {
        for (String role : roles) {
            if (this.role.equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role='" + role + "'}";
    }
}
