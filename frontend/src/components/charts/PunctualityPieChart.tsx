import React from 'react';
import { Pie } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, Title } from 'chart.js';
import { PunctualityStatsDto } from '@/types'; // Adjust import path as needed

ChartJS.register(ArcElement, Tooltip, Legend, Title);

interface PunctualityPieChartProps {
    data: PunctualityStatsDto | null;
    isLoading: boolean;
    error: string | null;
}

const PunctualityPieChart: React.FC<PunctualityPieChartProps> = ({ data, isLoading, error }) => {
    if (isLoading) return <p>Loading Punctuality Stats...</p>;
    if (error) return <p>Error loading stats: {error}</p>;
    if (!data) return <p>No punctuality data available.</p>;

    const chartData = {
        labels: ['Punctual', 'Late', 'Early', 'Missed'],
        datasets: [
            {
                label: 'Employee Punctuality',
                data: [data.punctualCount, data.lateCount, data.earlyCount, data.missedCount],
                backgroundColor: [
                    'rgba(75, 192, 192, 0.6)', // Teal/Green for Punctual
                    'rgba(255, 99, 132, 0.6)', // Red for Late
                    'rgba(54, 162, 235, 0.6)', // Blue for Early
                    'rgba(255, 159, 64, 0.6)', // Orange for Missed
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 159, 64, 1)',
                ],
                borderWidth: 1,
            },
        ],
    };

    const options = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: 'Overall Employee Punctuality',
            },
        },
    };

    // Avoid rendering chart if total is zero to prevent weird display
    if (data.totalRecords === 0) {
        return <p>No records found to display punctuality statistics.</p>;
    }

    return <Pie data={chartData} options={options} />;
};

export default PunctualityPieChart; 