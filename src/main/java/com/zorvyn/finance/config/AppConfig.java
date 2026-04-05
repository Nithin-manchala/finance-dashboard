package com.zorvyn.finance.config;

import com.zorvyn.finance.filter.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration class.
 *
 * We do NOT use Spring Security web (HttpSecurity, EnableWebSecurity etc.)
 * because we only added spring-security-crypto for BCrypt hashing —
 * that jar does NOT include the web security classes.
 *
 * Authentication is handled entirely by our own AuthFilter.
 * Authorization (role checks) is handled in the Service layer.
 *
 * This class has one job: register AuthFilter so that Tomcat
 * runs it on every /api/* request.
 */
@Configuration
public class AppConfig {

    @Autowired
    private AuthFilter authFilter;

    /**
     * Register our AuthFilter to run on every request path matching /api/*.
     *
     * FilterRegistrationBean lets us configure:
     *   - Which filter to register
     *   - What URL patterns it applies to
     *   - The order in which it runs (lower number = runs earlier)
     */
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/api/*");  // Apply to all /api/... routes
        registration.setOrder(1);               // Run this filter first
        return registration;
    }
}
