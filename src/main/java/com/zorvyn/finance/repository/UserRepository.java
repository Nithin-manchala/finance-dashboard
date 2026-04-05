package com.zorvyn.finance.repository;

import com.zorvyn.finance.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository class — responsible for ALL database operations related to users.
 *
 * We use JdbcTemplate (from Spring JDBC) which handles:
 *   - Opening/closing connections automatically
 *   - Wrapping SQL exceptions in cleaner Spring exceptions
 *   - Mapping ResultSet rows to our User objects via a RowMapper
 *
 * @Repository tells Spring: "This is a data access class — manage it as a bean"
 */
@Repository
public class UserRepository {

    // JdbcTemplate is the main JDBC helper — Spring creates it automatically
    // based on our application.properties datasource config
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper = a function that converts one database row (ResultSet) → User object.
     * We define this once and reuse it in all our query methods.
     *
     * rs = ResultSet (the raw database response)
     * rowNum = row index (we don't use it, but it's required by the interface)
     */
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    };

    // ─────────────────────────────────────────────────────────────────────────
    // USER QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Find a user by their email address.
     * Returns Optional.empty() if not found instead of null (safer pattern).
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        // The '?' is a parameter placeholder — JdbcTemplate replaces it safely
        // (prevents SQL injection attacks)
        List<User> users = jdbcTemplate.query(sql, userRowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Find a user by their auth token (used in our AuthFilter to identify who is calling).
     * We JOIN auth_tokens table to get the user associated with a given token.
     */
    public Optional<User> findByToken(String token) {
        String sql = """
                SELECT u.*
                FROM users u
                JOIN auth_tokens t ON u.id = t.user_id
                WHERE t.token = ?
                """;
        List<User> users = jdbcTemplate.query(sql, userRowMapper, token);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /** Find a user by their ID. */
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /** Get all users (Admin only operation). */
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY created_at DESC", userRowMapper);
    }

    /**
     * Save a new user to the database.
     * We use KeyHolder to capture the auto-generated ID from MySQL.
     */
    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password, role, status) VALUES (?, ?, ?, ?, ?)";

        // KeyHolder captures the auto-generated primary key after INSERT
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            // PreparedStatement with RETURN_GENERATED_KEYS tells MySQL to give us back the ID
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());    // Already BCrypt hashed before calling this
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus());
            return ps;
        }, keyHolder);

        // Set the generated ID back on the user object
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    /** Update a user's role (Admin only operation). */
    public void updateRole(Long userId, String role) {
        jdbcTemplate.update("UPDATE users SET role = ? WHERE id = ?", role, userId);
    }

    /** Update a user's status (ACTIVE / INACTIVE). */
    public void updateStatus(Long userId, String status) {
        jdbcTemplate.update("UPDATE users SET status = ? WHERE id = ?", status, userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOKEN MANAGEMENT (simple UUID-based auth)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new auth token for a user and stores it in the database.
     * Deletes any old tokens first (one active session per user).
     * Returns the generated token string.
     */
    public String createToken(Long userId) {
        // Delete existing tokens for this user (logout previous sessions)
        jdbcTemplate.update("DELETE FROM auth_tokens WHERE user_id = ?", userId);

        // Generate a random UUID as the token
        String token = UUID.randomUUID().toString();
        jdbcTemplate.update("INSERT INTO auth_tokens (user_id, token) VALUES (?, ?)", userId, token);

        return token;
    }

    /** Deletes a token from the database (used during logout). */
    public void deleteToken(String token) {
        jdbcTemplate.update("DELETE FROM auth_tokens WHERE token = ?", token);
    }

    /** Checks if an email already exists (used during registration). */
    public boolean emailExists(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }
}
