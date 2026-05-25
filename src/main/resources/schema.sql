-- ============================================================
-- FINANCE DASHBOARD - DATABASE SCHEMA
-- This file runs automatically every time the app starts.
-- Uses CREATE TABLE IF NOT EXISTS, so existing data is preserved.
-- ============================================================

-- USERS TABLE
-- Stores all users with their role and status
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)                       NOT NULL,
    email      VARCHAR(150)                       NOT NULL UNIQUE,
    password   VARCHAR(255)                       NOT NULL,       -- BCrypt hashed, never store plain text!
    role       ENUM('VIEWER','ANALYST','ADMIN')   NOT NULL DEFAULT 'VIEWER',
    status     ENUM('ACTIVE','INACTIVE')          NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP                          DEFAULT CURRENT_TIMESTAMP
);

-- AUTH TOKENS TABLE
-- Each login generates a UUID token stored here.
-- When a user logs out, their token is deleted.
-- NEW — safe version that works on all MySQL versions
CREATE TABLE IF NOT EXISTS auth_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- TRANSACTIONS TABLE
-- Stores all financial entries (income or expense)
CREATE TABLE IF NOT EXISTS transactions (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT                      NOT NULL,             -- Which user created this entry
    amount     DECIMAL(15, 2)              NOT NULL,             -- e.g. 5000.00
    type       ENUM('INCOME', 'EXPENSE')   NOT NULL,
    category   VARCHAR(100)               NOT NULL,             -- e.g. "Salary", "Rent", "Food"
    date       DATE                       NOT NULL,
    notes      TEXT,                                            -- Optional description
    is_deleted BOOLEAN                    NOT NULL DEFAULT FALSE,  -- Soft delete flag
    created_at TIMESTAMP                  DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
