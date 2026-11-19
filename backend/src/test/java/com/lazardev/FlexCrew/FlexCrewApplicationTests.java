package com.lazardev.FlexCrew;

import com.lazardev.FlexCrew.security.SecurityRulesTests;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Starts the whole application against PostgreSQL in Docker, without a local env.properties. */
@SpringBootTest
@Testcontainers
@TestPropertySource(
    properties = {
      "application.security.jwt.secret-key=" + SecurityRulesTests.TEST_KEY,
      "spring.jpa.properties.hibernate.default_schema=public",
      "spring.jpa.hibernate.ddl-auto=create"
    })
class FlexCrewApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Test
  void contextLoads() {}
}
