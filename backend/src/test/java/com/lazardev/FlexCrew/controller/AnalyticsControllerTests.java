package com.lazardev.FlexCrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.dto.analytics.PunctualityStatsDto;
import com.lazardev.FlexCrew.dto.analytics.ScheduleLoadDto;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Role;
import com.lazardev.FlexCrew.security.SecurityRulesTests;
import com.lazardev.FlexCrew.security.config.ApplicationConfig;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.config.SecurityConfiguration;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import({SecurityConfiguration.class, ApplicationConfig.class, JwtService.class})
@TestPropertySource(
        properties = "application.security.jwt.secret-key=" + SecurityRulesTests.TEST_KEY)
class AnalyticsControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtService jwtService;

        @MockBean
        private AnalyticsService analyticsService;

        @MockBean
        private EmployeeRepository employeeRepository;

        @MockBean
        private TokenRepository tokenRepository;

        @Autowired
        private ObjectMapper objectMapper; // For converting objects to JSON strings

        /** Signs a real JWT for an admin and stores it as a valid token, like a fresh login. */
        private String adminBearer() {
                Employee admin = Employee.builder()
                                .id(1)
                                .email("admin@test.local")
                                .role(Role.ADMIN)
                                .build();
                given(employeeRepository.findByEmail(admin.getEmail())).willReturn(Optional.of(admin));
                String jwt = jwtService.generateToken(admin);
                given(tokenRepository.findByToken(jwt))
                                .willReturn(Optional.of(Token.builder().token(jwt).employee(admin).build()));
                return "Bearer " + jwt;
        }

        @Test
        void getPunctualityStats_shouldReturnPunctualityDto_whenUserIsAdmin() throws Exception {
                PunctualityStatsDto punctualityDto = new PunctualityStatsDto(50L, 10L, 5L, 2L, 67L);
                given(analyticsService.getPunctualityStats()).willReturn(punctualityDto);

                mockMvc.perform(get("/api/v1/analytics/punctuality")
                                .header(HttpHeaders.AUTHORIZATION, adminBearer()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.punctualCount").value(50))
                                .andExpect(jsonPath("$.lateCount").value(10))
                                .andExpect(jsonPath("$.totalRecords").value(67));
        }

        @Test
        void getPunctualityStats_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
                mockMvc.perform(get("/api/v1/analytics/punctuality"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(analyticsService);
        }

        @Test
        void getScheduleLoadStats_shouldReturnScheduleLoadDtoList_whenUserIsAdmin() throws Exception {
                List<ScheduleLoadDto> scheduleLoadDtos = Arrays.asList(
                                new ScheduleLoadDto(1, "Morning Shift", 8, 10, 80.0),
                                new ScheduleLoadDto(2, "Evening Shift", 4, 5, 80.0));
                given(analyticsService.getScheduleLoadStats()).willReturn(scheduleLoadDtos);

                mockMvc.perform(get("/api/v1/analytics/schedule-load")
                                .header(HttpHeaders.AUTHORIZATION, adminBearer()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.size()").value(scheduleLoadDtos.size()))
                                .andExpect(jsonPath("$[0].scheduleName").value("Morning Shift"))
                                .andExpect(jsonPath("$[1].loadPercentage").value(80.0));
        }

        @Test
        void getScheduleLoadStats_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
                mockMvc.perform(get("/api/v1/analytics/schedule-load"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(analyticsService);
        }

}
