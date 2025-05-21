package com.lazardev.FlexCrew.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleLoadDto {
    private Integer scheduleId;
    private String scheduleName;
    private int currentEmployees;
    private int maxEmployees;
    private double loadPercentage; // Calculated as (currentEmployees / maxEmployees) * 100
}