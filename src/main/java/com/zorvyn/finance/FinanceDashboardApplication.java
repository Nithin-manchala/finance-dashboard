package com.zorvyn.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FinanceDashboardApplication — the entry point for our Spring Boot app.
 *
 * @SpringBootApplication is a shortcut that enables three things:
 *   1. @Configuration     — This class can define Spring beans
 *   2. @EnableAutoConfiguration — Spring Boot auto-configures things like
 *                                  DataSource, JdbcTemplate based on pom.xml + application.properties
 *   3. @ComponentScan     — Scans this package and all sub-packages for:
 *                                  @Component, @Service, @Repository, @RestController, etc.
 *
 * When you run main(), Spring Boot:
 *   1. Starts an embedded Tomcat web server
 *   2. Connects to MySQL using application.properties
 *   3. Runs schema.sql to create tables (if they don't exist)
 *   4. Registers all your controllers, services, repositories
 *   5. Runs DataInitializer to create the default admin user
 *   6. Starts listening for HTTP requests on port 8080
 */
@SpringBootApplication
public class FinanceDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceDashboardApplication.class, args);
        
        // Get environment details
        String port = System.getenv("PORT");
        if (port == null) port = "8080";
        String railwayEnv = System.getenv("RAILWAY_ENVIRONMENT");
        
        System.out.println("\n========================================================");
        System.out.println("  Finance Dashboard API is running!");
        System.out.println("  Port: " + port);
        if (railwayEnv != null) {
            System.out.println("  Environment: Railway - " + railwayEnv);
        } else {
            System.out.println("  Environment: Local Development");
        }
        System.out.println("  Base URL : /api");
        System.out.println("  Default Admin : admin@finance.com / admin123");
        System.out.println("========================================================\n");
    }
}
