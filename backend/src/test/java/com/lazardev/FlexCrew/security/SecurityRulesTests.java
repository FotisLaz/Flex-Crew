package com.lazardev.FlexCrew.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lazardev.FlexCrew.controller.AnalyticsController;
import com.lazardev.FlexCrew.controller.EmployeeController;
import com.lazardev.FlexCrew.controller.LoginController;
import com.lazardev.FlexCrew.controller.ProjectController;
import com.lazardev.FlexCrew.controller.RecordController;
import com.lazardev.FlexCrew.controller.ScheduleController;
import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.dto.analytics.PunctualityStatsDto;
import com.lazardev.FlexCrew.dto.analytics.ScheduleLoadDto;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Project;
import com.lazardev.FlexCrew.entity.Record;
import com.lazardev.FlexCrew.entity.Role;
import com.lazardev.FlexCrew.entity.Schedule;
import com.lazardev.FlexCrew.security.auth.AuthenticationService;
import com.lazardev.FlexCrew.security.config.ApplicationConfig;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.config.SecurityConfiguration;
import com.lazardev.FlexCrew.security.token.RefreshToken;
import com.lazardev.FlexCrew.security.token.RefreshTokenRepository;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.service.AnalyticsService;
import com.lazardev.FlexCrew.service.EmployeeServiceImpl;
import com.lazardev.FlexCrew.service.ProjectService;
import com.lazardev.FlexCrew.service.RecordService;
import com.lazardev.FlexCrew.service.ScheduleService;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Proves the authorization rules against the REAL security setup: {@link SecurityConfiguration},
 * the real {@code JwtAuthenticationFilter}, real JWTs signed by {@link JwtService}, and the real
 * authorities from {@link Employee#getAuthorities()}. Only persistence and business services are
 * mocked. No {@code @WithMockUser}: it would bypass {@code getAuthorities()} and hide role bugs.
 */
@WebMvcTest(
    controllers = {
      LoginController.class,
      EmployeeController.class,
      RecordController.class,
      ProjectController.class,
      ScheduleController.class,
      AnalyticsController.class
    })
@Import({
  SecurityConfiguration.class,
  ApplicationConfig.class,
  JwtService.class,
  AuthenticationService.class,
  EmployeeServiceImpl.class
})
@TestPropertySource(
    properties = "application.security.jwt.secret-key=" + SecurityRulesTests.TEST_KEY)
class SecurityRulesTests {

  /** Test-only signing key: base64 of "flexcrew-test-only-jwt-signing-key-not-a-real-secret". */
  static final String TEST_KEY =
      "ZmxleGNyZXctdGVzdC1vbmx5LWp3dC1zaWduaW5nLWtleS1ub3QtYS1yZWFsLXNlY3JldA==";

  static final String PASSWORD = "old-password";
  private static final String PASSWORD_HASH = new BCryptPasswordEncoder().encode(PASSWORD);

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtService jwtService;
  @Autowired private PasswordEncoder passwordEncoder;

  @MockBean private EmployeeRepository employeeRepository;
  @MockBean private TokenRepository tokenRepository;
  @MockBean private RefreshTokenRepository refreshTokenRepository;
  @MockBean private RecordService recordService;
  @MockBean private ProjectService projectService;
  @MockBean private ScheduleService scheduleService;
  @MockBean private AnalyticsService analyticsService;

  private Employee user; // id 1, USER
  private Employee admin; // id 2, ADMIN
  private Employee other; // id 3, USER

  /** In-memory stand-in for the Refresh_Tokens table, keyed by token hash. */
  private final Map<String, RefreshToken> refreshTokenTable = new HashMap<>();

  @BeforeEach
  void setUp() {
    user = employee(1, "user@test.local", Role.USER);
    admin = employee(2, "admin@test.local", Role.ADMIN);
    other = employee(3, "other@test.local", Role.USER);

    given(refreshTokenRepository.save(any(RefreshToken.class)))
        .willAnswer(
            invocation -> {
              RefreshToken row = invocation.getArgument(0);
              if (row.getId() == null) {
                row.setId(refreshTokenTable.size() + 1);
              }
              refreshTokenTable.put(row.getTokenHash(), row);
              return row;
            });
    given(refreshTokenRepository.findByTokenHash(anyString()))
        .willAnswer(
            invocation -> Optional.ofNullable(refreshTokenTable.get(invocation.getArgument(0))));
    given(refreshTokenRepository.revokeIfActive(anyInt()))
        .willAnswer(
            invocation -> {
              for (RefreshToken row : refreshTokenTable.values()) {
                if (row.getId().equals(invocation.getArgument(0)) && !row.isRevoked()) {
                  row.setRevoked(true);
                  return 1;
                }
              }
              return 0;
            });
    given(refreshTokenRepository.revokeAllByEmployeeId(anyInt()))
        .willAnswer(
            invocation -> {
              int revoked = 0;
              for (RefreshToken row : refreshTokenTable.values()) {
                if (row.getEmployee().getId().equals(invocation.getArgument(0))
                    && !row.isRevoked()) {
                  row.setRevoked(true);
                  revoked++;
                }
              }
              return revoked;
            });
  }

  private static Employee employee(int id, String email, Role role) {
    return Employee.builder()
        .id(id)
        .names("Test")
        .firstSurname("Person")
        .email(email)
        .password(PASSWORD_HASH)
        .role(role)
        .build();
  }

  /** Signs a real JWT for the employee and stores it as a valid token, like a fresh login. */
  private String bearer(Employee employee) {
    given(employeeRepository.findByEmail(employee.getEmail())).willReturn(Optional.of(employee));
    String jwt = jwtService.generateToken(employee);
    given(tokenRepository.findByToken(jwt))
        .willReturn(Optional.of(Token.builder().token(jwt).employee(employee).build()));
    return "Bearer " + jwt;
  }

  // ---------------------------------------------------------------------------------------------
  // 401: no token
  // ---------------------------------------------------------------------------------------------

  @Test
  void noToken_readEmployees_returns401() throws Exception {
    mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    verifyNoInteractions(employeeRepository);
  }

  @Test
  void noToken_readRecords_returns401() throws Exception {
    mockMvc.perform(get("/api/records/1")).andExpect(status().isUnauthorized());
    verifyNoInteractions(recordService);
  }

  @Test
  void noToken_deleteSchedule_returns401() throws Exception {
    mockMvc.perform(delete("/api/schedules/1")).andExpect(status().isUnauthorized());
    verifyNoInteractions(scheduleService);
  }

  @Test
  void validUserToken_readEmployees_returns200() throws Exception {
    given(employeeRepository.findAll()).willReturn(List.of(user, other));

    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------------------------
  // 401: a token is sent but cannot be trusted (must not be a 500)
  // ---------------------------------------------------------------------------------------------

  private static String signedToken(String subject, String base64Key, long expiresInMillis) {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Key));
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(subject)
        .issuedAt(new Date(now - 120_000))
        .expiration(new Date(now + expiresInMillis))
        .signWith(key)
        .compact();
  }

  @Test
  void expiredToken_returns401() throws Exception {
    String expired = signedToken(user.getEmail(), TEST_KEY, -60_000);
    // Everything else about the token is valid, so expiry is the only reason to reject it.
    given(employeeRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
    given(tokenRepository.findByToken(expired))
        .willReturn(Optional.of(Token.builder().token(expired).employee(user).build()));

    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
        .andExpect(status().isUnauthorized());
    verify(employeeRepository, never()).findAll();
  }

  @Test
  void malformedToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
        .andExpect(status().isUnauthorized());
    verify(employeeRepository, never()).findAll();
  }

  @Test
  void tokenSignedWithAnotherKey_returns401() throws Exception {
    String otherKey =
        Base64.getEncoder()
            .encodeToString("an-attacker-key-that-is-long-enough-for-hs256!!".getBytes());
    String forged = signedToken(admin.getEmail(), otherKey, 60_000);
    given(employeeRepository.findByEmail(admin.getEmail())).willReturn(Optional.of(admin));

    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
        .andExpect(status().isUnauthorized());
    verify(employeeRepository, never()).findAll();
  }

  @Test
  void tokenForUnknownUser_returns401() throws Exception {
    String ghost = signedToken("deleted@test.local", TEST_KEY, 60_000);
    given(employeeRepository.findByEmail("deleted@test.local")).willReturn(Optional.empty());

    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer " + ghost))
        .andExpect(status().isUnauthorized());
    verify(employeeRepository, never()).findAll();
  }

  /** Regression guard: this already worked before the filter fix. */
  @Test
  void revokedToken_returns401() throws Exception {
    given(employeeRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
    String jwt = jwtService.generateToken(user);
    given(tokenRepository.findByToken(jwt))
        .willReturn(Optional.of(Token.builder().token(jwt).employee(user).revoked(true).build()));

    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
        .andExpect(status().isUnauthorized());
    verify(employeeRepository, never()).findAll();
  }

  // ---------------------------------------------------------------------------------------------
  // Schedules: @PreAuthorize("hasRole('ADMIN')") must actually be enforced
  // ---------------------------------------------------------------------------------------------

  private static final String NEW_SCHEDULE_JSON =
      """
      {"name":"Morning","startTime":"09:00:00+00:00","endTime":"17:00:00+00:00","maxEmployees":5}""";
  private static final String SCHEDULE_1_JSON =
      """
      {"id":1,"name":"Morning","startTime":"09:00:00+00:00","endTime":"17:00:00+00:00",\
      "maxEmployees":5}""";

  @Test
  void user_createSchedule_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_SCHEDULE_JSON))
        .andExpect(status().isForbidden());
    verifyNoInteractions(scheduleService);
  }

  @Test
  void user_updateSchedule_returns403() throws Exception {
    mockMvc
        .perform(
            put("/api/schedules/1")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(SCHEDULE_1_JSON))
        .andExpect(status().isForbidden());
    verifyNoInteractions(scheduleService);
  }

  @Test
  void user_deleteSchedule_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/schedules/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(scheduleService);
  }

  @Test
  void user_assignEmployeeToSchedule_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/schedules/assign/3/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(scheduleService);
  }

  @Test
  void user_optimizeTeamSchedule_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/schedules/optimize/team/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(scheduleService);
  }

  @Test
  void admin_createSchedule_returns201() throws Exception {
    given(scheduleService.saveSchedule(any(Schedule.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_SCHEDULE_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  void admin_updateSchedule_returns200() throws Exception {
    mockMvc
        .perform(
            put("/api/schedules/1")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(SCHEDULE_1_JSON))
        .andExpect(status().isOk());
    verify(scheduleService).updateSchedule(any(Schedule.class));
  }

  @Test
  void admin_assignEmployeeToSchedule_returns200() throws Exception {
    given(scheduleService.assignEmployeeToSchedule(3, 1))
        .willReturn(Schedule.builder().id(1).name("Morning").build());

    mockMvc
        .perform(
            post("/api/schedules/assign/3/1").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isOk());
    verify(scheduleService).assignEmployeeToSchedule(3, 1);
  }

  @Test
  void admin_deleteSchedule_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/schedules/1").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isNoContent());
    verify(scheduleService).deleteScheduleById(1);
  }

  // ---------------------------------------------------------------------------------------------
  // Projects: reading requires login
  // ---------------------------------------------------------------------------------------------

  @Test
  void noToken_readProjects_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/projects")).andExpect(status().isUnauthorized());
    verifyNoInteractions(projectService);
  }

  @Test
  void noToken_readAssignmentSuggestions_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/projects/1/assignment-suggestions"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(projectService);
  }

  @Test
  void user_readProjects_returns200() throws Exception {
    given(projectService.getAllProjects()).willReturn(List.of());

    mockMvc
        .perform(get("/api/v1/projects").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk());
  }

  /**
   * Sending a token makes the browser preflight cross-origin calls. The preflight carries no
   * token, so it must be answered by CORS before authorization, or the Projects page breaks.
   */
  @Test
  void corsPreflight_forProjectsWithToken_isAllowed() throws Exception {
    mockMvc
        .perform(
            options("/api/v1/projects")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
  }

  // ---------------------------------------------------------------------------------------------
  // Registration: the client cannot choose its role
  // ---------------------------------------------------------------------------------------------

  @Test
  void register_ignoresClientSentAdminRole() throws Exception {
    given(employeeRepository.findByEmail("eve@test.local")).willReturn(Optional.empty());
    given(employeeRepository.save(any(Employee.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"names":"Eve","firstSurname":"Mallory","secondSurname":"X",\
                    "email":"eve@test.local","password":"pass-1234","role":"ADMIN"}"""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("USER"));

    ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
    verify(employeeRepository).save(saved.capture());
    assertEquals(Role.USER, saved.getValue().getRole());
  }

  // ---------------------------------------------------------------------------------------------
  // Projects and employees: only ADMIN may create, update or delete
  // ---------------------------------------------------------------------------------------------

  private static final String PROJECT_JSON = """
      {"name":"Apollo","description":"Test project"}""";
  private static final String NEW_ADMIN_EMPLOYEE_JSON =
      """
      {"names":"Mal","firstSurname":"Lory","email":"mal@test.local",\
      "password":"pass-1234","role":"ADMIN"}""";

  @Test
  void user_createProject_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/projects")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROJECT_JSON))
        .andExpect(status().isForbidden());
    verifyNoInteractions(projectService);
  }

  @Test
  void user_updateProject_returns403() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/projects/1")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROJECT_JSON))
        .andExpect(status().isForbidden());
    verifyNoInteractions(projectService);
  }

  @Test
  void user_deleteProject_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/v1/projects/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(projectService);
  }

  @Test
  void user_createEmployee_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/employees")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ADMIN_EMPLOYEE_JSON))
        .andExpect(status().isForbidden());
    verify(employeeRepository, never()).save(any(Employee.class));
  }

  @Test
  void admin_createProject_returns201() throws Exception {
    given(projectService.createProject(any(Project.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/v1/projects")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROJECT_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  void admin_updateProject_returns200() throws Exception {
    given(projectService.updateProject(eq(1), any(Project.class)))
        .willAnswer(invocation -> invocation.getArgument(1));

    mockMvc
        .perform(
            put("/api/v1/projects/1")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROJECT_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void admin_deleteProject_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/projects/1").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isNoContent());
    verify(projectService).deleteProject(1);
  }

  @Test
  void admin_createEmployee_returns201() throws Exception {
    given(employeeRepository.save(any(Employee.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/employees")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ADMIN_EMPLOYEE_JSON))
        .andExpect(status().isCreated());
  }

  // ---------------------------------------------------------------------------------------------
  // Analytics: company-wide statistics are ADMIN-only
  // ---------------------------------------------------------------------------------------------

  @Test
  void user_readPunctualityStats_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/analytics/punctuality").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(analyticsService);
  }

  @Test
  void user_readScheduleLoadStats_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/analytics/schedule-load").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(analyticsService);
  }

  @Test
  void admin_readPunctualityStats_returns200() throws Exception {
    given(analyticsService.getPunctualityStats())
        .willReturn(new PunctualityStatsDto(50L, 10L, 5L, 2L, 67L));

    mockMvc
        .perform(
            get("/api/v1/analytics/punctuality").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isOk());
  }

  @Test
  void admin_readScheduleLoadStats_returns200() throws Exception {
    given(analyticsService.getScheduleLoadStats())
        .willReturn(List.of(new ScheduleLoadDto(1, "Morning Shift", 8, 10, 80.0)));

    mockMvc
        .perform(
            get("/api/v1/analytics/schedule-load")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------------------------
  // Attendance records: an employee reads only their own; an admin reads anyone's
  // ---------------------------------------------------------------------------------------------

  @Test
  void user_readOtherEmployeesRecords_returns403() throws Exception {
    mockMvc
        .perform(get("/api/records/3").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(recordService);
  }

  @Test
  void user_readOwnRecords_returns200() throws Exception {
    given(recordService.findbyEmployeeId(1)).willReturn(List.of());

    mockMvc
        .perform(get("/api/records/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk());
  }

  @Test
  void admin_readOtherEmployeesRecords_returns200() throws Exception {
    given(recordService.findbyEmployeeId(3)).willReturn(List.of());

    mockMvc
        .perform(get("/api/records/3").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isOk());
  }

  @Test
  void user_searchOtherEmployeesRecordsByEmail_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/records/search")
                .param("employeeEmail", other.getEmail())
                .header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(recordService);
  }

  @Test
  void user_searchOwnRecordsByEmail_returns200() throws Exception {
    given(recordService.findbyEmployeeEmail(user.getEmail())).willReturn(List.of());

    mockMvc
        .perform(
            get("/api/records/search")
                .param("employeeEmail", user.getEmail())
                .header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk());
  }

  @Test
  void admin_searchOtherEmployeesRecordsByEmail_returns200() throws Exception {
    given(recordService.findbyEmployeeEmail(other.getEmail())).willReturn(List.of());

    mockMvc
        .perform(
            get("/api/records/search")
                .param("employeeEmail", other.getEmail())
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
        .andExpect(status().isOk());
  }

  @Test
  void user_countOtherEmployeesMonthlyRecords_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/records/searchbymonth")
                .param("employeeEmail", other.getEmail())
                .param("month", "5")
                .param("year", "2025")
                .header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(recordService);
  }

  @Test
  void user_countOwnMonthlyRecords_returns200() throws Exception {
    given(recordService.findbyEmployeeEmailAndMonth(user.getEmail(), 5, 2025)).willReturn(3);

    mockMvc
        .perform(
            get("/api/records/searchbymonth")
                .param("employeeEmail", user.getEmail())
                .param("month", "5")
                .param("year", "2025")
                .header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------------------------
  // Posting attendance: only for yourself, or by an admin (to correct records)
  // ---------------------------------------------------------------------------------------------

  private static String recordJsonFor(int employeeId) {
    return """
        {"employee":{"id":%d},"startTime":"2025-05-01T09:00:00+00:00"}"""
        .formatted(employeeId);
  }

  @Test
  void user_postRecordForOtherEmployee_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/records")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJsonFor(3)))
        .andExpect(status().isForbidden());
    verifyNoInteractions(recordService);
  }

  @Test
  void user_postOwnRecord_returns201() throws Exception {
    given(recordService.saveRecord(any(Record.class)))
        .willReturn(Record.builder().id(10).employee(user).build());

    mockMvc
        .perform(
            post("/api/records")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJsonFor(1)))
        .andExpect(status().isCreated());
  }

  @Test
  void admin_postRecordForOtherEmployee_returns201() throws Exception {
    given(recordService.saveRecord(any(Record.class)))
        .willReturn(Record.builder().id(11).employee(other).build());

    mockMvc
        .perform(
            post("/api/records")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJsonFor(3)))
        .andExpect(status().isCreated());
  }

  // ---------------------------------------------------------------------------------------------
  // Password hashes never leave the server
  // ---------------------------------------------------------------------------------------------

  @Test
  void employeeJson_doesNotContainPasswordHash() throws Exception {
    given(employeeRepository.findById(1)).willReturn(Optional.of(user));

    mockMvc
        .perform(get("/api/employees/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(user.getEmail()))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void recordJson_doesNotContainEmployeePasswordHash() throws Exception {
    given(recordService.findbyEmployeeId(1))
        .willReturn(List.of(Record.builder().id(5).employee(user).build()));

    mockMvc
        .perform(get("/api/records/1").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].employee.email").value(user.getEmail()))
        .andExpect(jsonPath("$[0].employee.password").doesNotExist());
  }

  /** Guards the choice of WRITE_ONLY over @JsonIgnore: a password must still be accepted. */
  @Test
  void admin_createEmployee_passwordIsStillReadFromJson() throws Exception {
    given(employeeRepository.save(any(Employee.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/employees")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ADMIN_EMPLOYEE_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist());

    ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
    verify(employeeRepository).save(saved.capture());
    assertNotNull(saved.getValue().getPassword());
  }

  // ---------------------------------------------------------------------------------------------
  // Admin-created employees get a hashed password and can log in
  // ---------------------------------------------------------------------------------------------

  /** Creates "mal@test.local" (password "pass-1234") as ADMIN and returns what was saved. */
  private Employee createEmployeeAsAdmin() throws Exception {
    given(employeeRepository.save(any(Employee.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/employees")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ADMIN_EMPLOYEE_JSON))
        .andExpect(status().isCreated());

    ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
    verify(employeeRepository).save(saved.capture());
    return saved.getValue();
  }

  @Test
  void admin_createEmployee_storesBCryptHash() throws Exception {
    Employee saved = createEmployeeAsAdmin();

    assertTrue(saved.getPassword().startsWith("$2"), "expected a BCrypt hash");
    assertTrue(passwordEncoder.matches("pass-1234", saved.getPassword()));
  }

  @Test
  void admin_createdEmployee_canLogIn() throws Exception {
    Employee saved = createEmployeeAsAdmin();
    given(employeeRepository.findByEmail("mal@test.local")).willReturn(Optional.of(saved));

    mockMvc
        .perform(
            post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"mal@test.local","password":"pass-1234"}"""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").isNotEmpty());
  }

  /** A client-sent id would make save() update that employee (role and password included). */
  @Test
  void admin_createEmployee_withExistingId_insertsInsteadOfOverwriting() throws Exception {
    given(employeeRepository.save(any(Employee.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/api/employees")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"id":3,"names":"Mal","firstSurname":"Lory","email":"mal@test.local",\
                    "password":"pass-1234","role":"ADMIN"}"""))
        .andExpect(status().isCreated());

    ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
    verify(employeeRepository).save(saved.capture());
    assertNull(saved.getValue().getId());
  }

  // ---------------------------------------------------------------------------------------------
  // Regression guards: these endpoints were already scoped to the caller; keep them that way
  // ---------------------------------------------------------------------------------------------

  private static String changePasswordJson(String email) {
    return """
        {"email":"%s","oldPassword":"%s","newPassword":"new-pass-1234"}"""
        .formatted(email, PASSWORD);
  }

  @Test
  void user_changeOtherEmployeesPassword_returns403() throws Exception {
    given(employeeRepository.findByEmail(other.getEmail())).willReturn(Optional.of(other));

    mockMvc
        .perform(
            put("/api/employees/change-password")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson(other.getEmail())))
        .andExpect(status().isForbidden());
    verify(employeeRepository, never()).save(any(Employee.class));
  }

  @Test
  void user_changeOwnPassword_returns200() throws Exception {
    mockMvc
        .perform(
            put("/api/employees/change-password")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson(user.getEmail())))
        .andExpect(status().isOk());

    ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
    verify(employeeRepository).save(saved.capture());
    assertEquals(user.getId(), saved.getValue().getId());
    assertTrue(passwordEncoder.matches("new-pass-1234", saved.getValue().getPassword()));
  }

  @Test
  void refreshToken_onlyAffectsTheTokensOwner() throws Exception {
    String refreshToken = storedRefreshToken(user);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/refresh-token")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
            .andExpect(status().isOk())
            .andReturn();

    String newAccessToken =
        JsonPath.read(result.getResponse().getContentAsString(), "$.access_token");
    assertEquals(user.getEmail(), jwtService.extractUsername(newAccessToken));
    verify(tokenRepository).findAllValidTokenByUser(user.getId());
    verify(tokenRepository, never()).findAllValidTokenByUser(other.getId());
    ArgumentCaptor<Token> savedToken = ArgumentCaptor.forClass(Token.class);
    verify(tokenRepository).save(savedToken.capture());
    assertEquals(user, savedToken.getValue().getEmployee());
  }

  /** iat/exp have second precision, so without a jti two tokens in one second were identical. */
  @Test
  void tokensIssuedInTheSameSecond_areUnique() {
    assertNotEquals(jwtService.generateRefreshToken(user), jwtService.generateRefreshToken(user));
    assertNotEquals(jwtService.generateToken(user), jwtService.generateToken(user));
  }

  private static String sha256Hex(String value) throws Exception {
    return HexFormat.of()
        .formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  /** Signs a real refresh token and stores its hash, as a login would. */
  private String storedRefreshToken(Employee employee) throws Exception {
    String refreshToken = jwtService.generateRefreshToken(employee);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .employee(employee)
            .tokenHash(sha256Hex(refreshToken))
            .expiresAt(Instant.now().plusSeconds(3600))
            .build());
    return refreshToken;
  }

  private String loginAndGetRefreshToken(Employee employee) throws Exception {
    given(employeeRepository.findByEmail(employee.getEmail())).willReturn(Optional.of(employee));
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"%s"}"""
                            .formatted(employee.getEmail(), PASSWORD)))
            .andExpect(status().isOk())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.refresh_token");
  }

  @Test
  void login_storesOnlyTheSha256HashOfTheRefreshToken() throws Exception {
    String refreshToken = loginAndGetRefreshToken(user);

    RefreshToken row = refreshTokenTable.get(sha256Hex(refreshToken));
    assertNotNull(row, "the refresh token's SHA-256 hash should be stored");
    assertEquals(user, row.getEmployee());
    assertFalse(row.isRevoked());
    assertFalse(refreshTokenTable.containsKey(refreshToken), "the raw token must not be stored");
  }

  @Test
  void refreshToken_expiresAfterSevenDays() throws Exception {
    String refreshToken = loginAndGetRefreshToken(user);

    Instant issuedAt = jwtService.extractClaim(refreshToken, Claims::getIssuedAt).toInstant();
    Instant expiresAt = jwtService.extractClaim(refreshToken, Claims::getExpiration).toInstant();
    assertEquals(Duration.ofDays(7), Duration.between(issuedAt, expiresAt));
    assertEquals(expiresAt, refreshTokenTable.get(sha256Hex(refreshToken)).getExpiresAt());
  }

  @Test
  void refreshToken_storedToken_returns200() throws Exception {
    refresh("Bearer " + loginAndGetRefreshToken(user))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").isNotEmpty());
  }

  private String refreshAndGetNewRefreshToken(String refreshToken) throws Exception {
    MvcResult result =
        refresh("Bearer " + refreshToken).andExpect(status().isOk()).andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.refresh_token");
  }

  @Test
  void refreshToken_returnsANewRefreshTokenAndRevokesTheOldOne() throws Exception {
    String first = loginAndGetRefreshToken(user);

    String second = refreshAndGetNewRefreshToken(first);

    assertNotEquals(first, second);
    assertTrue(refreshTokenTable.get(sha256Hex(first)).isRevoked());
    assertFalse(refreshTokenTable.get(sha256Hex(second)).isRevoked());
    refresh("Bearer " + second).andExpect(status().isOk());
  }

  @Test
  void refreshToken_usedTwice_secondUseReturns401() throws Exception {
    String first = loginAndGetRefreshToken(user);
    refreshAndGetNewRefreshToken(first);

    refresh("Bearer " + first).andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_reused_revokesAllOfTheUsersTokens() throws Exception {
    String first = loginAndGetRefreshToken(user);
    String second = refreshAndGetNewRefreshToken(first);
    String othersToken = storedRefreshToken(other);
    String accessToken = jwtService.generateToken(user);
    Token accessRow = Token.builder().token(accessToken).employee(user).build();
    given(tokenRepository.findByToken(accessToken)).willReturn(Optional.of(accessRow));
    given(tokenRepository.findAllValidTokenByUser(user.getId())).willReturn(List.of(accessRow));

    refresh("Bearer " + first).andExpect(status().isUnauthorized());

    refresh("Bearer " + second).andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isUnauthorized());
    assertFalse(refreshTokenTable.get(sha256Hex(othersToken)).isRevoked());
  }

  /** Two requests racing with one token: the one that loses the atomic revoke counts as reuse. */
  @Test
  void refreshToken_losingTheRevokeRace_revokesAllOfTheUsersTokens() throws Exception {
    String token = storedRefreshToken(user);
    String otherSession = storedRefreshToken(user);
    given(refreshTokenRepository.revokeIfActive(anyInt())).willReturn(0);

    refresh("Bearer " + token).andExpect(status().isUnauthorized());

    assertTrue(refreshTokenTable.get(sha256Hex(otherSession)).isRevoked());
  }

  @Test
  void logout_revokesTheRefreshTokenAndTheAccessToken() throws Exception {
    String refreshToken = loginAndGetRefreshToken(user);
    String accessToken = jwtService.generateToken(user);
    Token accessRow = Token.builder().token(accessToken).employee(user).build();
    given(tokenRepository.findByToken(accessToken)).willReturn(Optional.of(accessRow));
    given(tokenRepository.findAllValidTokenByUser(user.getId())).willReturn(List.of(accessRow));

    mockMvc
        .perform(
            post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
        .andExpect(status().isNoContent());

    refresh("Bearer " + refreshToken).andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/employees").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logout_withoutHeader_returns401() throws Exception {
    mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
  }

  @Test
  void logout_withAccessToken_returns401() throws Exception {
    mockMvc
        .perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(user)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_withAccessToken_returns401() throws Exception {
    refresh(bearer(user)).andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_validlySignedButNeverStored_returns401() throws Exception {
    given(employeeRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

    refresh("Bearer " + jwtService.generateRefreshToken(user))
        .andExpect(status().isUnauthorized());
  }

  private ResultActions refresh(String authHeader) throws Exception {
    MockHttpServletRequestBuilder request = post("/api/auth/refresh-token");
    if (authHeader != null) {
      request.header(HttpHeaders.AUTHORIZATION, authHeader);
    }
    return mockMvc.perform(request);
  }

  @Test
  void refreshToken_withoutHeader_returns401() throws Exception {
    refresh(null).andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_withNonBearerHeader_returns401() throws Exception {
    refresh("Basic dXNlcjpwYXNz").andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_malformed_returns401() throws Exception {
    refresh("Bearer not-a-jwt").andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_expired_returns401() throws Exception {
    given(employeeRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

    refresh("Bearer " + signedToken(user.getEmail(), TEST_KEY, -60_000))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_signedWithAnotherKey_returns401() throws Exception {
    String otherKey =
        Base64.getEncoder()
            .encodeToString("an-attacker-key-that-is-long-enough-for-hs256!!".getBytes());
    given(employeeRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

    refresh("Bearer " + signedToken(user.getEmail(), otherKey, 60_000))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_forUnknownUser_returns401() throws Exception {
    given(employeeRepository.findByEmail("deleted@test.local")).willReturn(Optional.empty());

    refresh("Bearer " + signedToken("deleted@test.local", TEST_KEY, 60_000))
        .andExpect(status().isUnauthorized());
  }
}
