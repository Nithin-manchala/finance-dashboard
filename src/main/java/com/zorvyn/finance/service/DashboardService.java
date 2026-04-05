package com.zorvyn.finance.service;

import com.zorvyn.finance.exception.AccessDeniedException;
import com.zorvyn.finance.model.Transaction;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardService — provides summary/analytics data for the dashboard.
 *
 * Access control:
 *   ANALYST and ADMIN → full access to all dashboard endpoints
 *   VIEWER → can only see the basic summary (total income/expense/net balance)
 */
@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Returns the full dashboard summary:
     *   - Total income
     *   - Total expenses
     *   - Net balance (income - expenses)
     *
     * All roles can access this basic overview.
     */
    public Map<String, Object> getSummary() {
        BigDecimal totalIncome  = transactionRepository.getTotalByType("INCOME");
        BigDecimal totalExpense = transactionRepository.getTotalByType("EXPENSE");
        BigDecimal netBalance   = totalIncome.subtract(totalExpense);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        // Net balance is positive if we earned more than we spent, negative otherwise
        summary.put("netBalance", netBalance);
        summary.put("status", netBalance.compareTo(BigDecimal.ZERO) >= 0 ? "SURPLUS" : "DEFICIT");

        return summary;
    }

    /**
     * Returns totals grouped by category and type.
     * Example: [{ category: "Salary", type: "INCOME", total: 50000, count: 2 }]
     *
     * Requires ANALYST or ADMIN role.
     */
    public List<Map<String, Object>> getCategoryWiseTotals(User currentUser) {
        requireAnalystOrAdmin(currentUser);
        return transactionRepository.getCategoryWiseTotals();
    }

    /**
     * Returns monthly income/expense totals for the last 12 months.
     * Useful for line charts/bar charts showing financial trends.
     *
     * Requires ANALYST or ADMIN role.
     */
    public List<Map<String, Object>> getMonthlyTrends(User currentUser) {
        requireAnalystOrAdmin(currentUser);
        return transactionRepository.getMonthlyTrends();
    }

    /**
     * Returns weekly income/expense totals for the past 4 weeks.
     *
     * Requires ANALYST or ADMIN role.
     */
    public List<Map<String, Object>> getWeeklyTrends(User currentUser) {
        requireAnalystOrAdmin(currentUser);
        return transactionRepository.getWeeklyTrends();
    }

    /**
     * Returns the N most recent transactions.
     * Useful for a "Recent Activity" feed on the dashboard.
     *
     * Requires ANALYST or ADMIN role.
     */
    public List<Transaction> getRecentActivity(int limit, User currentUser) {
        requireAnalystOrAdmin(currentUser);
        // Cap the limit to a max of 50 to prevent excessive data loading
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return transactionRepository.getRecent(safeLimit);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void requireAnalystOrAdmin(User user) {
        if (!user.hasRole("ANALYST", "ADMIN")) {
            throw new AccessDeniedException(
                    "Only Analysts and Admins can access detailed dashboard insights");
        }
    }
}
