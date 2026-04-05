package com.zorvyn.finance.config;

import com.zorvyn.finance.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer runs once, automatically, right after the Spring Boot
 * application starts and the database is ready.
 *
 * It implements ApplicationRunner — Spring calls the run() method automatically.
 *
 * What it does:
 *   Creates a default ADMIN user if none exists.
 *   Default credentials:
 *     Email:    admin@finance.com
 *     Password: admin123
 *
 * This ensures you always have an admin account to log in with on first startup.
 * You should change the password after the first login in a real deployment.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private AuthService authService;

    @Override
    public void run(ApplicationArguments args) {
        authService.createDefaultAdminIfNeeded();
    }
}
