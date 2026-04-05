package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.CreateTransactionRequest;
import com.zorvyn.finance.dto.UpdateTransactionRequest;
import com.zorvyn.finance.exception.AccessDeniedException;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.model.Transaction;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * TransactionService — business logic for financial records.
 *
 * Access control rules enforced here:
 *   CREATE  → ADMIN, ANALYST
 *   READ    → VIEWER, ANALYST, ADMIN (all roles)
 *   UPDATE  → ADMIN only
 *   DELETE  → ADMIN only
 *
 * Why put access control in the service and not the controller?
 * Because the service is the "heart" of the app. If the same logic
 * is ever called from multiple places (e.g., a batch job, a scheduler),
 * the access control rules are always enforced consistently.
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Create a new transaction.
     * Only ADMIN and ANALYST can create records.
     */
    public Transaction create(CreateTransactionRequest request, User currentUser) {
        // VIEWER cannot create transactions
        if (!currentUser.hasRole("ADMIN", "ANALYST")) {
            throw new AccessDeniedException("Only Admins and Analysts can create financial records");
        }

        Transaction t = new Transaction();
        t.setUserId(currentUser.getId());  // Track who created this record
        t.setAmount(request.getAmount());
        t.setType(request.getType().toUpperCase());
        t.setCategory(request.getCategory().trim());
        t.setDate(request.getDate());
        t.setNotes(request.getNotes());

        return transactionRepository.save(t);
    }

    /**
     * Get all transactions with optional filters.
     * All roles can view records.
     *
     * @param type     Filter by "INCOME" or "EXPENSE" (optional)
     * @param category Filter by category name (optional)
     * @param from     Filter records from this date (optional)
     * @param to       Filter records up to this date (optional)
     */
    public List<Transaction> getAll(String type, String category, LocalDate from, LocalDate to) {
        // Validate date range if both are provided
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }
        return transactionRepository.findAll(type, category, from, to);
    }

    /**
     * Get a single transaction by ID.
     * All roles can view individual records.
     */
    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }

    /**
     * Update an existing transaction.
     * Only ADMIN can modify existing records.
     */
    public Transaction update(Long id, UpdateTransactionRequest request, User currentUser) {
        if (!currentUser.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only Admins can update financial records");
        }

        // Verify the transaction exists before updating
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Update fields
        existing.setAmount(request.getAmount());
        existing.setType(request.getType().toUpperCase());
        existing.setCategory(request.getCategory().trim());
        existing.setDate(request.getDate());
        existing.setNotes(request.getNotes());

        transactionRepository.update(existing);
        return existing;
    }

    /**
     * Soft delete a transaction (mark as deleted, don't actually remove from DB).
     * Only ADMIN can delete records.
     *
     * Why soft delete?
     * - Preserves historical data and audit trails
     * - Allows recovery if something was deleted by mistake
     * - Financial records should never truly be lost
     */
    public void delete(Long id, User currentUser) {
        if (!currentUser.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only Admins can delete financial records");
        }

        // Verify the transaction exists
        transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        transactionRepository.softDelete(id);
    }
}
