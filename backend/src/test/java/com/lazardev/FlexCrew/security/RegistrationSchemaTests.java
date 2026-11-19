package com.lazardev.FlexCrew.security;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.security.auth.AuthenticationService;
import com.lazardev.FlexCrew.security.auth.RegisterRequest;
import com.lazardev.FlexCrew.security.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Registers against the schema that scripts/database-creation.sql creates (as Docker does), not
 * one generated from the entities, so column constraints in the script are part of the test.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({AuthenticationService.class, JwtService.class, BCryptPasswordEncoder.class})
@TestPropertySource(
    properties = {
      "application.security.jwt.secret-key=" + SecurityRulesTests.TEST_KEY,
      "spring.jpa.properties.hibernate.default_schema=Flex-Crew-v1",
      "spring.jpa.hibernate.ddl-auto=update"
    })
class RegistrationSchemaTests {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withCopyFileToContainer(
              MountableFile.forHostPath("../scripts/database-creation.sql"),
              "/docker-entrypoint-initdb.d/database-creation.sql");

  @Autowired private AuthenticationService authenticationService;
  @Autowired private EmployeeRepository employeeRepository;

  @MockBean private AuthenticationManager authenticationManager;

  @Test
  void registersWithoutASecondSurname() {
    authenticationService.register(
        RegisterRequest.builder()
            .names("Ana")
            .firstSurname("Lopez")
            .email("ana.lopez@test.local")
            .password("Passw0rd!")
            .build());

    employeeRepository.flush();
    assertNull(employeeRepository.findByEmail("ana.lopez@test.local").orElseThrow().getSecondSurname());
  }
}
