package com.lazardev.FlexCrew.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunctualityStatsDto {
    private long punctualCount;
    private long lateCount;
    private long earlyCount;
    private long missedCount; // Assuming 'Missed' is a status
    private long totalRecords;
    // Optionally, add fields for teamId or time period if needed later
}