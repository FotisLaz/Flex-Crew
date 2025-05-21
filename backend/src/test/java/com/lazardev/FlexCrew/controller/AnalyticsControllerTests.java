package com.lazardev.FlexCrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazardev.FlexCrew.dto.analytics.PunctualityStatsDto;
import com.lazardev.FlexCrew.dto.analytics.ScheduleLoadDto;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
// @Import(TestSecurityConfig.class) // Temporarily removed
class AnalyticsControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AnalyticsService analyticsService;

        @MockBean
        private JwtService jwtService; // Mock JwtService as it's likely a dependency for security filters

        @MockBean
        private TokenRepository tokenRepository;

        @MockBean
        private UserDetailsService userDetailsService;

        @Autowired
        private ObjectMapper objectMapper; // For converting objects to JSON strings

        @Test
        @WithMockUser(roles = "ADMIN") // Simulate an authenticated user with ADMIN role
        void getPunctualityStats_shouldReturnPunctualityDto_whenUserIsAdmin() throws Exception {
                PunctualityStatsDto punctualityDto = new PunctualityStatsDto(50L, 10L, 5L, 2L, 67L);
                given(analyticsService.getPunctualityStats()).willReturn(punctualityDto);

                mockMvc.perform(get("/api/v1/analytics/punctuality"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.punctualCount").value(50))
                                .andExpect(jsonPath("$.lateCount").value(10))
                                .andExpect(jsonPath("$.totalRecords").value(67));
        }

        @Test
        @WithMockUser // Simulate an authenticated user with default (USER) role
        void getPunctualityStats_shouldReturnPunctualityDto_whenUserIsAuthenticated() throws Exception {
                // Assuming the endpoint is now accessible to any authenticated user
                PunctualityStatsDto punctualityDto = new PunctualityStatsDto(50L, 10L, 5L, 2L, 67L);
                given(analyticsService.getPunctualityStats()).willReturn(punctualityDto);

                mockMvc.perform(get("/api/v1/analytics/punctuality"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.punctualCount").value(50));
        }

        @Test
        void getPunctualityStats_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
                // No @WithMockUser, so request is anonymous
                PunctualityStatsDto punctualityDto = new PunctualityStatsDto(50L, 10L, 5L, 2L, 67L);
                given(analyticsService.getPunctualityStats()).willReturn(punctualityDto); // Service might still be
                                                                                          // called if
                                                                                          // filter is earlier

                mockMvc.perform(get("/api/v1/analytics/punctuality"))
                                .andExpect(status().isUnauthorized()); // Changed from isForbidden()
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getScheduleLoadStats_shouldReturnScheduleLoadDtoList_whenUserIsAdmin() throws Exception {
                List<ScheduleLoadDto> scheduleLoadDtos = Arrays.asList(
                                new ScheduleLoadDto(1, "Morning Shift", 8, 10, 80.0),
                                new ScheduleLoadDto(2, "Evening Shift", 4, 5, 80.0));
                given(analyticsService.getScheduleLoadStats()).willReturn(scheduleLoadDtos);

                mockMvc.perform(get("/api/v1/analytics/schedule-load"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.size()").value(scheduleLoadDtos.size()))
                                .andExpect(jsonPath("$[0].scheduleName").value("Morning Shift"))
                                .andExpect(jsonPath("$[1].loadPercentage").value(80.0));
        }

        @Test
        @WithMockUser // Simulate an authenticated user with default (USER) role
        void getScheduleLoadStats_shouldReturnScheduleLoadDtoList_whenUserIsAuthenticated() throws Exception {
                // Assuming the endpoint is now accessible to any authenticated user
                List<ScheduleLoadDto> scheduleLoadDtos = Arrays.asList(
                                new ScheduleLoadDto(1, "Morning Shift", 8, 10, 80.0));
                given(analyticsService.getScheduleLoadStats()).willReturn(scheduleLoadDtos);

                mockMvc.perform(get("/api/v1/analytics/schedule-load"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.size()").value(1))
                                .andExpect(jsonPath("$[0].scheduleName").value("Morning Shift"));
        }

        @Test
        void getScheduleLoadStats_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
                // No @WithMockUser
                given(analyticsService.getScheduleLoadStats()).willReturn(Collections.emptyList());

                mockMvc.perform(get("/api/v1/analytics/schedule-load"))
                                .andExpect(status().isUnauthorized()); // Changed from isForbidden()
        }

}