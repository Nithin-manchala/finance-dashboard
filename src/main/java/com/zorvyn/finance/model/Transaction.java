package com.zorvyn.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a financial record (income or expense).
 * Maps to the 'transactions' table in MySQL.
 *
 * We use BigDecimal for 'amount' instead of double/float because
 * BigDecimal is precise — float/double can have rounding errors
 * which is dangerous when dealing with money!
 */
public class Transaction {

    private Long id;
    private Long userId;          // Who created this record
    private BigDecimal amount;    // e.g. 5000.00 (use BigDecimal for money!)
    private String type;          // "INCOME" or "EXPENSE"
    private String category;      // e.g. "Salary", "Rent", "Groceries"
    private LocalDate date;       // The date of the transaction
    private String notes;         // Optional description
    private Boolean isDeleted;    // Soft delete: true means "deleted" (but still in DB)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Transaction() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
