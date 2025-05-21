import React, { useState, useEffect } from 'react';
import { getPunctualityStats, getScheduleLoadStats } from '@/services/api/analyticsService'; // Adjust import path
import PunctualityPieChart from '@/components/charts/PunctualityPieChart'; // Adjust import path
import ScheduleLoadBarChart from '@/components/charts/ScheduleLoadBarChart'; // Adjust import path
import { PunctualityStatsDto, ScheduleLoadDto } from '@/types'; // Adjust import path
// import { useAuth } from '@/hooks/useAuth'; // Assuming you have an auth hook

const AdminDashboardPage: React.FC = () => {
    // const { user } = useAuth(); // Get user info to ensure ADMIN role (or handle in routing)

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
                const pData = await getPunctualityStats();
                setPunctualityData(pData);
                setPunctualityError(null);
            } catch (err: any) {
                console.error("Error fetching punctuality stats:", err);
                setPunctualityError(err.response?.data?.message || err.message || 'Failed to load punctuality stats');
            } finally {
                setPunctualityLoading(false);
            }

            // Fetch Schedule Load Stats
            try {
                setScheduleLoadLoading(true);
                const sData = await getScheduleLoadStats();
                setScheduleLoadData(sData);
                setScheduleLoadError(null);
            } catch (err: any) {
                console.error("Error fetching schedule load stats:", err);
                setScheduleLoadError(err.response?.data?.message || err.message || 'Failed to load schedule load stats');
            } finally {
                setScheduleLoadLoading(false);
            }
        };

        fetchData();
    }, []);

    // Optional: Add a check here to ensure only ADMINs see this page, if not handled by routing
    // if (user?.role !== 'ADMIN') {
    //     return <p>Access Denied. You must be an admin to view this page.</p>;
    // }

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