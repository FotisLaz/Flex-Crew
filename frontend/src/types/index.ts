/**
 * Represents the structure of punctuality statistics data
 * received from the backend analytics endpoint.
 */
export interface PunctualityStatsDto {
    punctualCount: number;
    lateCount: number;
    earlyCount: number;
    missedCount: number; // Assuming 'Missed' is a status name from backend
    totalRecords: number;
}

/**
 * Represents the structure of schedule load data for a single schedule
 * received from the backend analytics endpoint.
 */
export interface ScheduleLoadDto {
    scheduleId: number;      // Changed from Integer to number
    scheduleName: string;
    currentEmployees: number;
    maxEmployees: number;
    loadPercentage: number;  // Changed from double to number
}

// Add other shared types or interfaces here as needed 