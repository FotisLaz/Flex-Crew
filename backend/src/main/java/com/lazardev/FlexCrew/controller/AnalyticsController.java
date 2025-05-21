package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.dto.analytics.PunctualityStatsDto;
import com.lazardev.FlexCrew.dto.analytics.ScheduleLoadDto;
import com.lazardev.FlexCrew.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics") // Base path for analytics endpoints
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/punctuality")
    public ResponseEntity<PunctualityStatsDto> getPunctualityStats() {
        PunctualityStatsDto stats = analyticsService.getPunctualityStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/schedule-load")
    public ResponseEntity<List<ScheduleLoadDto>> getScheduleLoadStats() {
        List<ScheduleLoadDto> stats = analyticsService.getScheduleLoadStats();
        return ResponseEntity.ok(stats);
    }

    // Add more endpoints corresponding to methods in AnalyticsService
}