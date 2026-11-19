import React, { useState, useEffect } from 'react';
import { getPunctualityStats, getScheduleLoadStats } from '@/services/api/analyticsService';
import PunctualityPieChart from '@/components/charts/PunctualityPieChart';
import ScheduleLoadBarChart from '@/components/charts/ScheduleLoadBarChart';
import { PunctualityStatsDto, ScheduleLoadDto } from '@/types';
import { getErrorMessage } from '../../api/getErrorMessage';
import useApiPrivate from '@/hooks/useApiPrivate';

const AdminDashboardPage: React.FC = () => {
    const apiPrivate = useApiPrivate();

    const [punctualityData, setPunctualityData] = useState<PunctualityStatsDto | null>(null);
    const [scheduleLoadData, setScheduleLoadData] = useState<ScheduleLoadDto[] | null>(null);
    const [punctualityLoading, setPunctualityLoading] = useState<boolean>(true);
    const [scheduleLoadLoading, setScheduleLoadLoading] = useState<boolean>(true);
    const [punctualityError, setPunctualityError] = useState<string | null>(null);
    const [scheduleLoadError, setScheduleLoadError] = useState<string | null>(null);

    useEffect(() => {
        const fetchData = async () => {
            // Fetch Punctuality Stats
            try {
                setPunctualityLoading(true);
                const pData = await getPunctualityStats(apiPrivate);
                setPunctualityData(pData);
                setPunctualityError(null);
            } catch (err) {
                console.error("Error fetching punctuality stats:", err);
                setPunctualityError(getErrorMessage(err, 'Failed to load punctuality stats'));
            } finally {
                setPunctualityLoading(false);
            }

            // Fetch Schedule Load Stats
            try {
                setScheduleLoadLoading(true);
                const sData = await getScheduleLoadStats(apiPrivate);
                setScheduleLoadData(sData);
                setScheduleLoadError(null);
            } catch (err) {
                console.error("Error fetching schedule load stats:", err);
                setScheduleLoadError(getErrorMessage(err, 'Failed to load schedule load stats'));
            } finally {
                setScheduleLoadLoading(false);
            }
        };

        fetchData();
    }, [apiPrivate]);

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">Admin Dashboard</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-white p-4 rounded shadow">
                    <PunctualityPieChart
                        data={punctualityData}
                        isLoading={punctualityLoading}
                        error={punctualityError}
                    />
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <ScheduleLoadBarChart
                        data={scheduleLoadData}
                        isLoading={scheduleLoadLoading}
                        error={scheduleLoadError}
                    />
                </div>
            </div>

            {/* Add more charts/stats components here as needed */}

        </div>
    );
};

export default AdminDashboardPage; 