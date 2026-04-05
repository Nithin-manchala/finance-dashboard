package com.zorvyn.finance.repository;

import com.zorvyn.finance.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for all financial transaction database operations.
 *
 * Key concept used here: Dynamic Query Building
 * Since users can filter transactions by type, category, and date range,
 * we build the SQL query dynamically based on which filters are provided.
 */
@Repository
public class TransactionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper that converts a database row into a Transaction object.
     * Notice we use rs.getDate("date").toLocalDate() to convert SQL Date → Java LocalDate.
     */
    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> {
        Transaction t = new Transaction();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setType(rs.getString("type"));
        t.setCategory(rs.getString("category"));
        t.setDate(rs.getDate("date").toLocalDate());
        t.setNotes(rs.getString("notes"));
        t.setIsDeleted(rs.getBoolean("is_deleted"));
        // Handle nullable timestamps
        if (rs.getTimestamp("created_at") != null)
            t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            t.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return t;
    };

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /** Save a new transaction and return it with the generated ID. */
    public Transaction save(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, amount, type, category, date, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, t.getUserId());
            ps.setBigDecimal(2, t.getAmount());
            ps.setString(3, t.getType());
            ps.setString(4, t.getCategory());
            // Convert Java LocalDate → SQL Date for MySQL
            ps.setDate(5, Date.valueOf(t.getDate()));
            ps.setString(6, t.getNotes());
            return ps;
        }, keyHolder);

        t.setId(keyHolder.getKey().longValue());
        return t;
    }

    /**
     * Find all non-deleted transactions with optional filters.
     *
     * This is dynamic query building:
     *   - We start with the base query
     *   - If a filter is provided (not null/empty), we append an AND clause
     *   - We collect the corresponding parameter values in a list
     *   - At the end, we pass the built SQL and params to JdbcTemplate
     */
    public List<Transaction> findAll(String type, String category, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM transactions WHERE is_deleted = FALSE"
        );
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            params.add(type.toUpperCase());
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (from != null) {
            sql.append(" AND date >= ?");
            params.add(Date.valueOf(from));  // Convert LocalDate → SQL Date
        }
        if (to != null) {
            sql.append(" AND date <= ?");
            params.add(Date.valueOf(to));
        }

        sql.append(" ORDER BY date DESC");

        // params.toArray() converts our List<Object> → Object[]
        return jdbcTemplate.query(sql.toString(), transactionRowMapper, params.toArray());
    }

    /** Find a single transaction by ID (only returns non-deleted ones). */
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ? AND is_deleted = FALSE";
        List<Transaction> result = jdbcTemplate.query(sql, transactionRowMapper, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /** Update all fields of an existing transaction. */
    public void update(Transaction t) {
        String sql = "UPDATE transactions SET amount=?, type=?, category=?, date=?, notes=?, " +
                     "updated_at=NOW() WHERE id=?";
        jdbcTemplate.update(sql,
                t.getAmount(),
                t.getType(),
                t.getCategory(),
                Date.valueOf(t.getDate()),
                t.getNotes(),
                t.getId()
        );
    }

    /**
     * Soft delete: mark a transaction as deleted WITHOUT actually removing it from the database.
     * This preserves historical data and audit trails.
     * The is_deleted = TRUE flag hides it from all query results.
     */
    public void softDelete(Long id) {
        jdbcTemplate.update(
                "UPDATE transactions SET is_deleted = TRUE, updated_at = NOW() WHERE id = ?",
                id
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD / ANALYTICS QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the total sum of all transactions of a given type (INCOME or EXPENSE).
     * COALESCE(SUM(amount), 0) means: if there are no rows, return 0 instead of NULL.
     */
    public BigDecimal getTotalByType(String type) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                     "WHERE type = ? AND is_deleted = FALSE";
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, type);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Returns total amount grouped by category and type.
     * Used for the category-wise breakdown on the dashboard.
     *
     * Returns a List of Maps — each Map represents one row:
     *   [{"category": "Salary", "type": "INCOME", "total": 50000.00}, ...]
     */
    public List<Map<String, Object>> getCategoryWiseTotals() {
        String sql = """
                SELECT category, type, SUM(amount) AS total, COUNT(*) AS count
                FROM transactions
                WHERE is_deleted = FALSE
                GROUP BY category, type
                ORDER BY total DESC
                """;
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Returns income and expense totals grouped by month (last 12 months).
     * DATE_FORMAT(date, '%Y-%m') formats date as "2024-01", "2024-02", etc.
     * Used for monthly trend charts on the dashboard.
     */
    public List<Map<String, Object>> getMonthlyTrends() {
        String sql = """
                SELECT DATE_FORMAT(date, '%Y-%m') AS month, type, SUM(amount) AS total
                FROM transactions
                WHERE is_deleted = FALSE
                GROUP BY month, type
                ORDER BY month DESC
                LIMIT 24
                """;
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Returns the N most recently added transactions.
     * 'limit' controls how many to return (default: 10 in DashboardService).
     */
    public List<Transaction> getRecent(int limit) {
        String sql = "SELECT * FROM transactions WHERE is_deleted = FALSE " +
                     "ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, transactionRowMapper, limit);
    }

    /**
     * Returns weekly totals for the past 4 weeks.
     * YEARWEEK() is a MySQL function that groups dates by calendar week.
     */
    public List<Map<String, Object>> getWeeklyTrends() {
        String sql = """
                SELECT YEARWEEK(date, 1) AS week, type, SUM(amount) AS total
                FROM transactions
                WHERE is_deleted = FALSE AND date >= DATE_SUB(NOW(), INTERVAL 4 WEEK)
                GROUP BY week, type
                ORDER BY week DESC
                """;
        return jdbcTemplate.queryForList(sql);
    }
}
