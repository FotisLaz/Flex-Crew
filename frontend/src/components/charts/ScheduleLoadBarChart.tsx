import React from 'react';
import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { ScheduleLoadDto } from '@/types'; // Adjust import path as needed

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

interface ScheduleLoadBarChartProps {
    data: ScheduleLoadDto[] | null;
    isLoading: boolean;
    error: string | null;
}

const ScheduleLoadBarChart: React.FC<ScheduleLoadBarChartProps> = ({ data, isLoading, error }) => {
    if (isLoading) return <p>Loading Schedule Load Stats...</p>;
    if (error) return <p>Error loading stats: {error}</p>;
    if (!data || data.length === 0) return <p>No schedule load data available.</p>;

    const chartData = {
        labels: data.map(item => item.scheduleName),
        datasets: [
            {
                label: 'Current Employees',
                data: data.map(item => item.currentEmployees),
                backgroundColor: 'rgba(54, 162, 235, 0.6)', // Blue
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1,
            },
            {
                label: 'Max Employees',
                data: data.map(item => item.maxEmployees),
                backgroundColor: 'rgba(255, 99, 132, 0.6)', // Red
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1,
            },
            // Optional: Add load percentage as another bar or on a different axis
            // {
            //     label: 'Load %',
            //     data: data.map(item => item.loadPercentage),
            //     backgroundColor: 'rgba(75, 192, 192, 0.6)', // Teal
            //     yAxisID: 'y1', // Requires configuring a second Y axis
            // },
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
                text: 'Employee Load per Schedule',
            },
            tooltip: {
                callbacks: {
                    // Optional: customize tooltip
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Schedule'
                }
            },
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Number of Employees'
                },
            },
            // Optional: Second Y axis for percentage
            // y1: {
            //     type: 'linear' as const,
            //     display: true,
            //     position: 'right' as const,
            //     min: 0,
            //     max: 100,
            //     grid: {
            //         drawOnChartArea: false, // only want the grid lines for absolute counts
            //     },
            //     title: {
            //         display: true,
            //         text: 'Load Percentage (%)'
            //     }
            // },
        },
    };

    return <Bar data={chartData} options={options} />;
};

export default ScheduleLoadBarChart; 