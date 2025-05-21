import { apiPrivate } from './apiClient'; // Assuming you have a configured axios instance
import { PunctualityStatsDto, ScheduleLoadDto } from '@/types'; // Χρησιμοποιώντας το path alias

// Define the expected DTO structures based on backend
// You might need to create these types in a 'types' folder if they don't exist
// export interface PunctualityStatsDto {
//     punctualCount: number;
//     lateCount: number;
//     earlyCount: number;
//     missedCount: number;
//     totalRecords: number;
// }
//
// export interface ScheduleLoadDto {
//     scheduleId: number;
//     scheduleName: string;
//     currentEmployees: number;
//     maxEmployees: number;
//     loadPercentage: number;
// }

/**
 * Fetches punctuality statistics from the backend.
 * Requires ADMIN role.
 * Uses apiPrivate for authenticated request.
 */
export const getPunctualityStats = async (): Promise<PunctualityStatsDto> => {
    const response = await apiPrivate.get<PunctualityStatsDto>('/analytics/punctuality');
    return response.data;
};

/**
 * Fetches schedule load statistics from the backend.
 * Requires ADMIN role.
 * Uses apiPrivate for authenticated request.
 */
export const getScheduleLoadStats = async (): Promise<ScheduleLoadDto[]> => {
    const response = await apiPrivate.get<ScheduleLoadDto[]>('/analytics/schedule-load');
    return response.data;
}; 