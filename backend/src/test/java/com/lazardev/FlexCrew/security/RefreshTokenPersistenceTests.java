package com.lazardev.FlexCrew.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.security.auth.AuthenticationResponse;
import com.lazardev.FlexCrew.security.auth.AuthenticationService;
import com.lazardev.FlexCrew.security.auth.RegisterRequest;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.token.RefreshToken;
import com.lazardev.FlexCrew.security.token.RefreshTokenRepository;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the refresh-token queries and transactions against a real PostgreSQL in Docker. The test
 * methods are deliberately not transactional: a test-wide transaction would hide a rollback of
 * the revocations made right before the 401 on reuse.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({AuthenticationService.class, JwtService.class, BCryptPasswordEncoder.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(
    properties = {
      "application.security.jwt.secret-key=" + SecurityRulesTests.TEST_KEY,
      "spring.jpa.properties.hibernate.default_schema=public",
      "spring.jpa.hibernate.ddl-auto=create"
    })
class RefreshTokenPersistenceTests {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private AuthenticationService authenticationService;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private PlatformTransactionManager transactionManager;

  @MockBean private AuthenticationManager authenticationManager;

  @BeforeEach
  void cleanDatabase() {
    refreshTokenRepository.deleteAll();
    tokenRepository.deleteAll();
    employeeRepository.deleteAll();
  }

  private AuthenticationResponse register(String email) {
    return authenticationService.register(
        RegisterRequest.builder()
            .names("Test")
            .firstSurname("Person")
            .secondSurname("X")
            .email(email)
            .password("pass-1234")
            .build());
  }

  private Integer employeeId(String email) {
    return employeeRepository.findByEmail(email).orElseThrow().getId();
  }

  private static String sha256Hex(String value) throws Exception {
    return HexFormat.of()
        .formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void onlyTheHashOfTheRefreshTokenIsStored() throws Exception {
    String refreshToken = register("user@test.local").getRefreshToken();

    List<RefreshToken> rows = refreshTokenRepository.findAll();
    assertEquals(1, rows.size());
    assertEquals(sha256Hex(refreshToken), rows.get(0).getTokenHash());
    assertNotEquals(refreshToken, rows.get(0).getTokenHash());
  }

  /** Each call runs in its own transaction, like two requests presenting the same token. */
  @Test
  void revokeIfActive_succeedsOnlyOnce() {
    register("user@test.local");
    Integer id = refreshTokenRepository.findAll().get(0).getId();
    TransactionTemplate inNewTransaction = new TransactionTemplate(transactionManager);

    int firstUse = inNewTransaction.execute(status -> refreshTokenRepository.revokeIfActive(id));
    int secondUse = inNewTransaction.execute(status -> refreshTokenRepository.revokeIfActive(id));

    assertEquals(1, firstUse);
    assertEquals(0, secondUse);
  }

  @Test
  void reusedRefreshToken_revokesAllOfTheUsersTokens_andTheRevocationIsCommitted() {
    String first = register("user@test.local").getRefreshToken();
    register("other@test.local");
    authenticationService.refreshToken("Bearer " + first);

    ResponseStatusException reuse =
        assertThrows(
            ResponseStatusException.class,
            () -> authenticationService.refreshToken("Bearer " + first));
    assertEquals(HttpStatus.UNAUTHORIZED, reuse.getStatusCode());

    Integer userId = employeeId("user@test.local");
    for (RefreshToken row : refreshTokenRepository.findAll()) {
      boolean belongsToUser = row.getEmployee().getId().equals(userId);
      assertEquals(belongsToUser, row.isRevoked(), "refresh token " + row.getId());
    }
    for (Token row : tokenRepository.findAll()) {
      boolean belongsToUser = row.getEmployee().getId().equals(userId);
      assertEquals(belongsToUser, row.isRevoked(), "access token " + row.getId());
    }
  }

  @Test
  void rotatedRefreshToken_isStoredAndActive() throws Exception {
    String first = register("user@test.local").getRefreshToken();

    String second = authenticationService.refreshToken("Bearer " + first).getRefreshToken();

    RefreshToken firstRow =
        refreshTokenRepository.findByTokenHash(sha256Hex(first)).orElseThrow();
    RefreshToken secondRow =
        refreshTokenRepository.findByTokenHash(sha256Hex(second)).orElseThrow();
    assertTrue(firstRow.isRevoked());
    assertFalse(secondRow.isRevoked());
  }
}
