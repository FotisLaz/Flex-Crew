import { AxiosInstance } from 'axios';
import { PunctualityStatsDto, ScheduleLoadDto } from '@/types';

/**
 * Fetches punctuality statistics from the backend.
 * Requires ADMIN role. Pass the client from useApiPrivate().
 */
export const getPunctualityStats = async (api: AxiosInstance): Promise<PunctualityStatsDto> => {
    const response = await api.get<PunctualityStatsDto>('/api/v1/analytics/punctuality');
    return response.data;
};

/**
 * Fetches schedule load statistics from the backend.
 * Requires ADMIN role. Pass the client from useApiPrivate().
 */
export const getScheduleLoadStats = async (api: AxiosInstance): Promise<ScheduleLoadDto[]> => {
    const response = await api.get<ScheduleLoadDto[]>('/api/v1/analytics/schedule-load');
    return response.data;
};
