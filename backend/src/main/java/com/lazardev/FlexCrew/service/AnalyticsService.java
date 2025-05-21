package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.dao.IssueRepository;
import com.lazardev.FlexCrew.dao.RecordRepository;
import com.lazardev.FlexCrew.dao.ScheduleRepository;
import com.lazardev.FlexCrew.dao.projection.PunctualityCount;
import com.lazardev.FlexCrew.dto.analytics.PunctualityStatsDto;
import com.lazardev.FlexCrew.dto.analytics.ScheduleLoadDto;
import com.lazardev.FlexCrew.entity.Schedule; // Assuming Schedule entity import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    // Inject necessary repositories
    private final RecordRepository recordRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository; // Keep if needed for other stats
    private final IssueRepository issueRepository; // Keep if needed for other stats

    // Method to get overall punctuality stats
    public PunctualityStatsDto getPunctualityStats() {
        List<PunctualityCount> counts = recordRepository.countRecordsByIssueStatus();
        Map<String, Long> countsMap = counts.stream()
                .collect(Collectors.toMap(PunctualityCount::getStatusName, PunctualityCount::getCount));

        // Assuming status names exactly match these strings based on SQL script
        long punctual = countsMap.getOrDefault("Punctual", 0L);
        long late = countsMap.getOrDefault("Late", 0L);
        long early = countsMap.getOrDefault("Early", 0L);
        long missed = countsMap.getOrDefault("Missed", 0L);

        // Calculate total from the individual counts to ensure consistency
        long total = punctual + late + early + missed;
        // Or, if you want total records regardless of issue status, use
        // recordRepository.count()
        // long total = recordRepository.count();

        return new PunctualityStatsDto(punctual, late, early, missed, total);
    }

    // Method to get schedule load stats
    public List<ScheduleLoadDto> getScheduleLoadStats() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return schedules.stream()
                .map(schedule -> {
                    double loadPercentage = 0.0;
                    // Ensure currentEmployees and maxEmployees are not null before using them
                    int currentEmployees = schedule.getCurrentEmployees() != null ? schedule.getCurrentEmployees() : 0;
                    int maxEmployees = schedule.getMaxEmployees() != null ? schedule.getMaxEmployees() : 0;

                    if (maxEmployees > 0) {
                        loadPercentage = (double) currentEmployees / maxEmployees * 100;
                    }
                    return new ScheduleLoadDto(
                            schedule.getId(),
                            schedule.getName(),
                            currentEmployees,
                            maxEmployees,
                            loadPercentage);
                })
                .collect(Collectors.toList());
    }

    // Add more methods for other analytics as needed (e.g., stats per team)
}