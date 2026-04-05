package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.model.Transaction;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DashboardController — analytics and summary endpoints.
 *
 * Endpoints:
 *   GET /api/dashboard/summary           → Total income, expenses, net balance (all roles)
 *   GET /api/dashboard/categories        → Category-wise totals (ANALYST, ADMIN)
 *   GET /api/dashboard/trends/monthly    → Monthly income/expense trends (ANALYST, ADMIN)
 *   GET /api/dashboard/trends/weekly     → Weekly trends (ANALYST, ADMIN)
 *   GET /api/dashboard/recent            → Recent transactions feed (ANALYST, ADMIN)
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get the basic financial summary.
     * Available to ALL authenticated users (VIEWER, ANALYST, ADMIN).
     *
     * Response example:
     * {
     *   "totalIncome": 100000.00,
     *   "totalExpense": 60000.00,
     *   "netBalance": 40000.00,
     *   "status": "SURPLUS"
     * }
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        Map<String, Object> summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary fetched", summary));
    }

    /**
     * Get totals grouped by category.
     * Requires ANALYST or ADMIN role.
     *
     * Response example:
     * [
     *   { "category": "Salary", "type": "INCOME", "total": 80000, "count": 2 },
     *   { "category": "Rent",   "type": "EXPENSE", "total": 15000, "count": 1 }
     * ]
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategoryTotals(
            HttpServletRequest request) {

        User currentUser = (User) request.getAttribute("currentUser");
        List<Map<String, Object>> data = dashboardService.getCategoryWiseTotals(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Category totals fetched", data));
    }

    /**
     * Get monthly income/expense trends.
     * Requires ANALYST or ADMIN role.
     *
     * Response example:
     * [
     *   { "month": "2024-03", "type": "INCOME",  "total": 50000 },
     *   { "month": "2024-03", "type": "EXPENSE", "total": 30000 }
     * ]
     */
    @GetMapping("/trends/monthly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyTrends(
            HttpServletRequest request) {

        User currentUser = (User) request.getAttribute("currentUser");
        List<Map<String, Object>> data = dashboardService.getMonthlyTrends(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Monthly trends fetched", data));
    }

    /**
     * Get weekly income/expense trends (past 4 weeks).
     * Requires ANALYST or ADMIN role.
     */
    @GetMapping("/trends/weekly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWeeklyTrends(
            HttpServletRequest request) {

        User currentUser = (User) request.getAttribute("currentUser");
        List<Map<String, Object>> data = dashboardService.getWeeklyTrends(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Weekly trends fetched", data));
    }

    /**
     * Get recent activity feed.
     * Requires ANALYST or ADMIN role.
     *
     * Query param: limit (optional, default 10, max 50)
     * URL example: /api/dashboard/recent?limit=5
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Transaction>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> data = dashboardService.getRecentActivity(limit, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Recent activity fetched", data));
    }
}
