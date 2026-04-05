package com.zorvyn.finance.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AuthFilter — runs on EVERY incoming HTTP request.
 *
 * This is a Servlet Filter. It intercepts requests BEFORE they reach
 * any controller, and it can either:
 *   1. Allow the request through (if the token is valid)
 *   2. Reject it with 401 Unauthorized (if no token or invalid token)
 *
 * How our token system works:
 *   - Client logs in → gets a UUID token
 *   - Client includes it in every request: Authorization: Bearer <token>
 *   - This filter reads the token, finds the user, and attaches them to the request
 *   - Controllers read the user via request.getAttribute("currentUser")
 *
 * OncePerRequestFilter = Spring guarantees this filter runs exactly once per request.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    // ObjectMapper converts Java objects → JSON string (for the error response)
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * These paths do NOT require authentication.
     * Requests to these paths skip the token check entirely.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ── Skip auth for public endpoints ─────────────────────────────────
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);  // Pass through without checking
            return;
        }

        // ── Extract token from Authorization header ────────────────────────
        // Header format: "Authorization: Bearer abc123-uuid"
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing or invalid Authorization header. " +
                    "Format: Authorization: Bearer <token>");
            return;
        }

        // Extract just the token part (remove "Bearer " prefix)
        String token = authHeader.substring(7).trim();

        // ── Look up the user associated with this token ───────────────────
        Optional<User> userOpt = userRepository.findByToken(token);

        if (userOpt.isEmpty()) {
            sendUnauthorized(response, "Invalid or expired token. Please login again.");
            return;
        }

        User user = userOpt.get();

        // Check if the user's account is still active
        if (!user.isActive()) {
            sendUnauthorized(response, "Your account has been deactivated.");
            return;
        }

        // ── Attach the user to the request ────────────────────────────────
        // Any controller can now call: User user = (User) request.getAttribute("currentUser")
        request.setAttribute("currentUser", user);

        // Continue to the next filter / controller
        filterChain.doFilter(request, response);
    }

    /**
     * Sends a 401 Unauthorized JSON response.
     * We manually write JSON here since we're in a filter (not a controller).
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    // 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("data", null);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
