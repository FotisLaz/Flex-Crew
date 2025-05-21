import React, { useState, useEffect } from 'react';
import { Schedule } from '../../../types/Schedule';

interface ScheduleFormProps {
    initialData?: Schedule | null; // Pass existing data for editing, null for creating
    onSubmit: (scheduleData: Omit<Schedule, 'id' | 'currentEmployees'> & { id?: number }) => Promise<void>; // Function to call on successful submit
    onCancel: () => void; // Function to call when cancelling
    isSaving: boolean; // To show loading state on submit button
}

const ScheduleForm: React.FC<ScheduleFormProps> = ({ initialData, onSubmit, onCancel, isSaving }) => {
    const [name, setName] = useState('');
    const [startTime, setStartTime] = useState(''); // Store as HH:MM string
    const [endTime, setEndTime] = useState('');   // Store as HH:MM string
    const [maxEmployees, setMaxEmployees] = useState<number | ''>(1); // Default to 1, allow empty string during input
    const [formError, setFormError] = useState<string | null>(null);

    // Populate form if initialData is provided (for editing)
    useEffect(() => {
        if (initialData) {
            setName(initialData.name || '');
            setStartTime(formatTimeForInput(initialData.startTime));
            setEndTime(formatTimeForInput(initialData.endTime));
            setMaxEmployees(initialData.maxEmployees || 1);
        } else {
            // Reset form for creating new
            setName('');
            setStartTime('');
            setEndTime('');
            setMaxEmployees(1);
        }
        setFormError(null); // Clear errors when form loads/reloads
    }, [initialData]);

    // Helper to format time from backend (e.g., "HH:MM:SS<offset>") to "HH:MM"
    const formatTimeForInput = (timeString: string | null | undefined): string => {
        if (!timeString) return '';
        try {
            // Assuming timeString is like "HH:MM:SS<offset>" or just "HH:MM:SS"
            const parts = timeString.split(':');
            if (parts.length >= 2) {
                return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
            }
            return '';
        } catch (e) {
            console.error("Error parsing time for input:", timeString, e);
            return '';
        }
    };

    // Helper to format time for backend (append seconds and a default offset if needed)
    const formatTimeForBackend = (timeString: string): string => {
        if (!timeString || !timeString.includes(':')) return '00:00:00+00:00'; // Or throw error
        return `${timeString}:00+00:00`; // Append seconds and default offset - ADJUST IF BACKEND EXPECTS DIFFERENT FORMAT
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setFormError(null);

        // Basic Validation
        if (!name.trim()) {
            setFormError('Schedule name is required.');
            return;
        }
        if (!startTime) {
            setFormError('Start time is required.');
            return;
        }
        if (!endTime) {
            setFormError('End time is required.');
            return;
        }
         if (maxEmployees === '' || maxEmployees <= 0) {
            setFormError('Maximum employees must be a positive number.');
            return;
        }

        // Construct data object matching backend expectations (adjust as needed)
        const scheduleData: Omit<Schedule, 'id' | 'currentEmployees'> & { id?: number } = {
            name: name.trim(),
            startTime: formatTimeForBackend(startTime),
            endTime: formatTimeForBackend(endTime),
            maxEmployees: Number(maxEmployees), // Convert to number
        };

        // Add id if we are editing
        if (initialData?.id) {
            scheduleData.id = initialData.id;
        }

        try {
            await onSubmit(scheduleData);
            // Let the parent component handle closing the modal on success
        } catch (error: any) {
            console.error("Error submitting schedule form:", error);
            setFormError(error.message || "An error occurred while saving the schedule.");
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {formError && <p className="text-red-500 text-sm bg-red-100 p-2 rounded">{formError}</p>}

            <div>
                <label htmlFor="scheduleName" className="block text-sm font-medium text-gray-700 mb-1">Schedule Name</label>
                <input 
                    type="text"
                    id="scheduleName"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="e.g., Morning Shift"
                />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                    <label htmlFor="startTime" className="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
                    <input 
                        type="time"
                        id="startTime"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>
                <div>
                    <label htmlFor="endTime" className="block text-sm font-medium text-gray-700 mb-1">End Time</label>
                    <input 
                        type="time"
                        id="endTime"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>
            </div>

            <div>
                <label htmlFor="maxEmployees" className="block text-sm font-medium text-gray-700 mb-1">Max Employees</label>
                <input 
                    type="number"
                    id="maxEmployees"
                    value={maxEmployees}
                    onChange={(e) => setMaxEmployees(e.target.value === '' ? '' : Number(e.target.value))}
                    required
                    min="1"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="e.g., 5"
                />
            </div>

            {/* Form Actions */}
            <div className="flex justify-end pt-4 space-x-2">
                <button 
                    type="button" 
                    onClick={onCancel} 
                    className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 disabled:opacity-50"
                    disabled={isSaving}
                >
                    Cancel
                </button>
                <button 
                    type="submit" 
                    className={`px-4 py-2 ${initialData ? 'bg-blue-600 hover:bg-blue-700' : 'bg-green-600 hover:bg-green-700'} text-white rounded-md disabled:opacity-50 flex items-center justify-center ${isSaving ? 'cursor-not-allowed' : ''}`}
                    disabled={isSaving}
                >
                    {isSaving ? (
                        <div className="animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></div>
                    ) : null}
                    {isSaving ? 'Saving...' : (initialData ? 'Update Schedule' : 'Create Schedule')}
                </button>
            </div>
        </form>
    );
};

export default ScheduleForm; 