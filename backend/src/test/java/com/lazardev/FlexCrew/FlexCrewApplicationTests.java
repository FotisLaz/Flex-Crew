package com.lazardev.FlexCrew;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lazardev.FlexCrew.security.SecurityRulesTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Starts the whole application against PostgreSQL in Docker, without a local env.properties. */
@SpringBootTest
@AutoConfigureMockMvc
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

  @Autowired private MockMvc mockMvc;

  @Test
  void contextLoads() {}

  @Test
  void healthIsPublicAndShowsOnlyTheStatus() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.components").doesNotExist());
  }

  @Test
  void otherActuatorPathsNeedAToken() throws Exception {
    mockMvc.perform(get("/actuator/info")).andExpect(status().isUnauthorized());
    mockMvc.perform(get("/actuator/health/db")).andExpect(status().isUnauthorized());
  }
}
