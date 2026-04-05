package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.CreateTransactionRequest;
import com.zorvyn.finance.dto.UpdateTransactionRequest;
import com.zorvyn.finance.model.Transaction;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * TransactionController — all endpoints for managing financial records.
 *
 * Endpoints:
 *   POST   /api/transactions           → Create a new transaction (ANALYST, ADMIN)
 *   GET    /api/transactions           → Get all transactions, with optional filters (all roles)
 *   GET    /api/transactions/{id}      → Get one transaction (all roles)
 *   PUT    /api/transactions/{id}      → Update a transaction (ADMIN only)
 *   DELETE /api/transactions/{id}      → Soft-delete a transaction (ADMIN only)
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Create a new financial record.
     *
     * HTTP Method: POST
     * URL: /api/transactions
     * Body example:
     * {
     *   "amount": 5000.00,
     *   "type": "INCOME",
     *   "category": "Salary",
     *   "date": "2024-01-15",
     *   "notes": "January salary"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> create(
            @Valid @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = (User) httpRequest.getAttribute("currentUser");
        Transaction created = transactionService.create(request, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(ApiResponse.success("Transaction created successfully", created));
    }

    /**
     * Get all transactions with optional query parameter filters.
     *
     * HTTP Method: GET
     * URL examples:
     *   /api/transactions                          → All records
     *   /api/transactions?type=INCOME              → Only income records
     *   /api/transactions?category=Rent            → Only "Rent" category
     *   /api/transactions?from=2024-01-01&to=2024-03-31  → Date range
     *   /api/transactions?type=EXPENSE&category=Food    → Combined filters
     *
     * @RequestParam(required = false) means the parameter is optional.
     * @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) parses "2024-01-15" into a LocalDate.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<Transaction> transactions = transactionService.getAll(type, category, from, to);
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched " + transactions.size() + " transaction(s)", transactions));
    }

    /**
     * Get a single transaction by its ID.
     *
     * HTTP Method: GET
     * URL: /api/transactions/5
     * {id} in the URL is captured by @PathVariable.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> getById(@PathVariable Long id) {
        Transaction transaction = transactionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched", transaction));
    }

    /**
     * Update an existing transaction (ADMIN only).
     *
     * HTTP Method: PUT
     * URL: /api/transactions/5
     * Body: Full transaction object with updated values.
     *
     * We use PUT (not PATCH) because we replace ALL fields, not just some.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = (User) httpRequest.getAttribute("currentUser");
        Transaction updated = transactionService.update(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", updated));
    }

    /**
     * Soft-delete a transaction (ADMIN only).
     *
     * HTTP Method: DELETE
     * URL: /api/transactions/5
     *
     * The record is NOT removed from the database — it is marked as deleted.
     * This preserves historical/audit data.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        User currentUser = (User) httpRequest.getAttribute("currentUser");
        transactionService.delete(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }
}
