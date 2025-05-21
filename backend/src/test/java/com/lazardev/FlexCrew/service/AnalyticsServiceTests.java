package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.dao.RecordRepository;
import com.lazardev.FlexCrew.dao.ScheduleRepository;
import com.lazardev.FlexCrew.dao.projection.PunctualityCount;
import com.lazardev.FlexCrew.dto.analytics.PunctualityStatsDto;
import com.lazardev.FlexCrew.dto.analytics.ScheduleLoadDto;
import com.lazardev.FlexCrew.entity.Schedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTests {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    // Helper class/interface for PunctualityCount
    static class MockPunctualityCount implements PunctualityCount {
        private final String statusName;
        private final Long count;

        public MockPunctualityCount(String statusName, Long count) {
            this.statusName = statusName;
            this.count = count;
        }

        @Override
        public String getStatusName() {
            return statusName;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }

    @Test
    void getPunctualityStats_shouldReturnCorrectStats_whenAllCountsPresent() {
        List<PunctualityCount> counts = Arrays.asList(
                new MockPunctualityCount("Punctual", 50L),
                new MockPunctualityCount("Late", 10L),
                new MockPunctualityCount("Early", 5L),
                new MockPunctualityCount("Missed", 2L));
        when(recordRepository.countRecordsByIssueStatus()).thenReturn(counts);

        PunctualityStatsDto stats = analyticsService.getPunctualityStats();

        assertEquals(50L, stats.getPunctualCount());
        assertEquals(10L, stats.getLateCount());
        assertEquals(5L, stats.getEarlyCount());
        assertEquals(2L, stats.getMissedCount());
        assertEquals(67L, stats.getTotalRecords());
    }

    @Test
    void getPunctualityStats_shouldReturnCorrectStats_whenSomeCountsMissing() {
        List<PunctualityCount> counts = Arrays.asList(
                new MockPunctualityCount("Punctual", 30L),
                new MockPunctualityCount("Late", 7L));
        when(recordRepository.countRecordsByIssueStatus()).thenReturn(counts);

        PunctualityStatsDto stats = analyticsService.getPunctualityStats();

        assertEquals(30L, stats.getPunctualCount());
        assertEquals(7L, stats.getLateCount());
        assertEquals(0L, stats.getEarlyCount()); // Expect 0 if not present
        assertEquals(0L, stats.getMissedCount()); // Expect 0 if not present
        assertEquals(37L, stats.getTotalRecords());
    }

    @Test
    void getPunctualityStats_shouldReturnZeroCounts_whenRepositoryReturnsEmptyList() {
        when(recordRepository.countRecordsByIssueStatus()).thenReturn(Collections.emptyList());

        PunctualityStatsDto stats = analyticsService.getPunctualityStats();

        assertEquals(0L, stats.getPunctualCount());
        assertEquals(0L, stats.getLateCount());
        assertEquals(0L, stats.getEarlyCount());
        assertEquals(0L, stats.getMissedCount());
        assertEquals(0L, stats.getTotalRecords());
    }

    @Test
    void getPunctualityStats_shouldIgnoreUnknownStatusNames() {
        List<PunctualityCount> counts = Arrays.asList(
                new MockPunctualityCount("Punctual", 20L),
                new MockPunctualityCount("UnknownStatus", 5L) // This should be ignored
        );
        when(recordRepository.countRecordsByIssueStatus()).thenReturn(counts);

        PunctualityStatsDto stats = analyticsService.getPunctualityStats();

        assertEquals(20L, stats.getPunctualCount());
        assertEquals(0L, stats.getLateCount());
        assertEquals(0L, stats.getEarlyCount());
        assertEquals(0L, stats.getMissedCount());
        assertEquals(20L, stats.getTotalRecords());
    }

    @Test
    void getScheduleLoadStats_shouldReturnCorrectLoad_whenSchedulesPresent() {
        Schedule schedule1 = new Schedule(1, "Morning", null, null, 10, 8); // id, name, start, end, max, current
        Schedule schedule2 = new Schedule(2, "Evening", null, null, 5, 5);
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);
        when(scheduleRepository.findAll()).thenReturn(schedules);

        List<ScheduleLoadDto> loadStats = analyticsService.getScheduleLoadStats();

        assertEquals(2, loadStats.size());
        ScheduleLoadDto dto1 = loadStats.get(0);
        assertEquals(1, dto1.getScheduleId());
        assertEquals("Morning", dto1.getScheduleName());
        assertEquals(8, dto1.getCurrentEmployees());
        assertEquals(10, dto1.getMaxEmployees());
        assertEquals(80.0, dto1.getLoadPercentage(), 0.01);

        ScheduleLoadDto dto2 = loadStats.get(1);
        assertEquals(2, dto2.getScheduleId());
        assertEquals("Evening", dto2.getScheduleName());
        assertEquals(5, dto2.getCurrentEmployees());
        assertEquals(5, dto2.getMaxEmployees());
        assertEquals(100.0, dto2.getLoadPercentage(), 0.01);
    }

    @Test
    void getScheduleLoadStats_shouldHandleZeroMaxEmployeesGracefully() {
        Schedule schedule1 = new Schedule(1, "ZeroMax", null, null, 0, 5);
        when(scheduleRepository.findAll()).thenReturn(Collections.singletonList(schedule1));

        List<ScheduleLoadDto> loadStats = analyticsService.getScheduleLoadStats();

        assertEquals(1, loadStats.size());
        ScheduleLoadDto dto1 = loadStats.get(0);
        assertEquals(1, dto1.getScheduleId());
        assertEquals("ZeroMax", dto1.getScheduleName());
        assertEquals(5, dto1.getCurrentEmployees());
        assertEquals(0, dto1.getMaxEmployees());
        assertEquals(0.0, dto1.getLoadPercentage(), 0.01); // Expect 0% load if max is 0
    }

    @Test
    void getScheduleLoadStats_shouldHandleNullMaxEmployeesGracefully() {
        Schedule schedule1 = new Schedule(1, "NullMax", null, null, null, 5);
        when(scheduleRepository.findAll()).thenReturn(Collections.singletonList(schedule1));

        List<ScheduleLoadDto> loadStats = analyticsService.getScheduleLoadStats();

        assertEquals(1, loadStats.size());
        ScheduleLoadDto dto1 = loadStats.get(0);
        assertEquals(1, dto1.getScheduleId());
        assertEquals("NullMax", dto1.getScheduleName());
        assertEquals(5, dto1.getCurrentEmployees());
        assertEquals(0, dto1.getMaxEmployees()); // Null max should be treated as 0
        assertEquals(0.0, dto1.getLoadPercentage(), 0.01);
    }

    @Test
    void getScheduleLoadStats_shouldHandleNullCurrentEmployeesGracefully() {
        Schedule schedule1 = new Schedule(1, "NullCurrent", null, null, 10, null);
        when(scheduleRepository.findAll()).thenReturn(Collections.singletonList(schedule1));

        List<ScheduleLoadDto> loadStats = analyticsService.getScheduleLoadStats();

        assertEquals(1, loadStats.size());
        ScheduleLoadDto dto1 = loadStats.get(0);
        assertEquals(1, dto1.getScheduleId());
        assertEquals("NullCurrent", dto1.getScheduleName());
        assertEquals(0, dto1.getCurrentEmployees()); // Null current should be treated as 0
        assertEquals(10, dto1.getMaxEmployees());
        assertEquals(0.0, dto1.getLoadPercentage(), 0.01);
    }

    @Test
    void getScheduleLoadStats_shouldHandleNullCurrentAndMaxEmployeesGracefully() {
        Schedule schedule1 = new Schedule(1, "NullBoth", null, null, null, null);
        when(scheduleRepository.findAll()).thenReturn(Collections.singletonList(schedule1));

        List<ScheduleLoadDto> loadStats = analyticsService.getScheduleLoadStats();

        assertEquals(1, loadStats.size());
        ScheduleLoadDto dto1 = loadStats.get(0);
        assertEquals(1, dto1.getScheduleId());
        assertEquals("NullBoth", dto1.getScheduleName());
        assertEquals(0, dto1.getCurrentEmployees());
        assertEquals(0, dto1.getMaxEmployees());
        assertEquals(0.0, dto1.getLoadPercentage(), 0.01);
    }

    @Test
    void getScheduleLoadStats_shouldReturnEmptyList_whenRepositoryReturnsEmptyList() {
        when(scheduleRepository.findAll()).thenReturn(Collections.emptyList());

        List<ScheduleLoadDto> loadStats = analyticsService.getScheduleLoadStats();

        assertTrue(loadStats.isEmpty());
    }
}