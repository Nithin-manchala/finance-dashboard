package com.zorvyn.finance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic smoke test — verifies that the Spring application context
 * loads without errors.
 *
 * @SpringBootTest loads the full application context.
 * @TestPropertySource overrides the datasource with an in-memory H2 database
 * so tests don't need a real MySQL instance.
 *
 * Note: To run tests, add the H2 dependency to pom.xml (test scope):
 *   <dependency>
 *       <groupId>com.h2database</groupId>
 *       <artifactId>h2</artifactId>
 *       <scope>test</scope>
 *   </dependency>
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class FinanceDashboardApplicationTests {

    @Test
    void contextLoads() {
        // If the application context loads without throwing an exception, this test passes.
        // It verifies that all beans are correctly wired together.
    }
}
