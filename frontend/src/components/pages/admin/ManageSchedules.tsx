import React, { useState, useEffect } from 'react';
import { Schedule } from '../../types/Schedule';
import useApiPrivate from '../../../hooks/useApiPrivate';
import { BsPlusCircle, BsPencilSquare, BsTrash, BsClock, BsPeople } from 'react-icons/bs';
import Grid from '../../common/Grid';
import Navbar from '../../common/Navbar';
import Modal from '../../common/Modal';
import ScheduleForm from './ScheduleForm';

const ManageSchedules: React.FC = () => {
    const [schedules, setSchedules] = useState<Schedule[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const apiPrivate = useApiPrivate();

    // State for managing the Create/Edit modal
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedSchedule, setSelectedSchedule] = useState<Schedule | null>(null);
    const [isSaving, setIsSaving] = useState(false); // For form submit loading state
    
    // --- State for Delete Confirmation Modal (To be added later if needed) ---
    // const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
    // const [scheduleToDelete, setScheduleToDelete] = useState<Schedule | null>(null);

    const fetchSchedules = async (controller?: AbortController) => {
        setLoading(true);
        setError(null);
        try {
            const response = await apiPrivate.get('/api/schedules', { signal: controller?.signal });
            setSchedules(response.data || []);
        } catch (err: any) {
            console.error("Error fetching schedules:", err);
            if (err.name !== 'AbortError') {
                setError(err.response?.data?.message || err.message || 'Failed to fetch schedules.');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const controller = new AbortController();
        fetchSchedules(controller);
        return () => {
            controller.abort();
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // Removed apiPrivate dependency if fetchSchedules doesn't change based on it

    // --- Handlers for opening modals ---
    const handleCreate = () => {
        setSelectedSchedule(null); // Ensure we are creating new
        setIsModalOpen(true);
    };

    const handleEdit = (schedule: Schedule) => {
        setSelectedSchedule(schedule); // Set the schedule to edit
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedSchedule(null); // Clear selection on close
        setError(null); // Clear any previous form errors shown in modal
        setIsSaving(false); // Reset saving state
    };

    // --- Form Submission Logic ---
    const handleFormSubmit = async (scheduleData: Omit<Schedule, 'id' | 'currentEmployees'> & { id?: number }) => {
        setIsSaving(true);
        setError(null);
        const isEditing = !!selectedSchedule; // Check if we are editing or creating
        const url = isEditing ? `/api/schedules/${selectedSchedule.id}` : '/api/schedules';
        const method = isEditing ? 'put' : 'post';

        try {
            const response = await apiPrivate[method](url, scheduleData);

            if (isEditing) {
                // Update the schedule in the list
                setSchedules(schedules.map(s => s.id === selectedSchedule.id ? { ...s, ...response.data } : s));
            } else {
                // Add the new schedule to the list
                setSchedules([...schedules, response.data]);
            }
            handleCloseModal(); // Close modal on success
        } catch (err: any) {
            console.error(`Error ${isEditing ? 'updating' : 'creating'} schedule:`, err);
            // Display error within the modal (could be passed down to ScheduleForm or shown here)
            setError(err.response?.data?.message || err.message || `Failed to ${isEditing ? 'update' : 'create'} schedule.`);
            // Optionally, re-throw or handle specific errors
        } finally {
            setIsSaving(false);
        }
    };

    // --- Delete Logic (Placeholder - Needs Confirmation Modal) ---
    const handleDelete = async (schedule: Schedule) => {
        console.log("Attempting to delete schedule (no confirmation yet):", schedule.id);
        // TODO: Implement confirmation modal first
        const confirmDelete = window.confirm(`Are you sure you want to delete the schedule "${schedule.name}"? This action cannot be undone.`);
        
        if (confirmDelete) {
             try {
                 await apiPrivate.delete(`/api/schedules/${schedule.id}`);
                 // Remove the schedule from the list optimistically or after confirmation
                 setSchedules(schedules.filter(s => s.id !== schedule.id));
                 console.log("Schedule deleted successfully");
             } catch (err: any) {
                 console.error("Failed to delete schedule:", err);
                 setError(err.response?.data?.message || err.message || "Failed to delete schedule.");
                 // Show error message to the user (e.g., using a toast notification or an alert)
                 alert(`Error deleting schedule: ${error}`); // Simple alert for now
             }
        }
    };
    
    /* 
    const confirmDelete = async () => {
         console.log("Deleting schedule...")
        // if (!scheduleToDelete) return;
        // try {
        //     await apiPrivate.delete(`/api/schedules/${scheduleToDelete.id}`);
        //     setSchedules(schedules.filter(s => s.id !== scheduleToDelete.id));
        //     setIsDeleteConfirmOpen(false);
        //     setScheduleToDelete(null);
        // } catch (err) {
        //     console.error("Failed to delete schedule:", err);
        //     // Show error message to user
        // }
    };
    */

    // Helper to format time (HH:MM)
    const formatTime = (timeString: string | null | undefined): string => {
        if (!timeString) return 'N/A';
        try {
            const timePart = timeString.split(':');
            return `${timePart[0].padStart(2,'0')}:${timePart[1].padStart(2,'0')}`;
        } catch (e) {
            console.error("Error formatting time:", timeString, e);
            return 'Invalid Time';
        }
    };

    return (
        <Grid>
            <Navbar />
            <div className="p-6 bg-white rounded-lg shadow-md h-full flex flex-col">
                <div className="flex justify-between items-center mb-6 pb-3 border-b">
                    <h1 className="text-2xl font-bold text-gray-800">Manage Schedules</h1>
                    <button 
                        onClick={handleCreate}
                        className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors text-sm"
                    >
                        <BsPlusCircle />
                        New Schedule
                    </button>
                </div>

                {loading && (
                    <div className="flex justify-center items-center flex-grow">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600"></div>
                    </div>
                )}

                {error && !loading && !isModalOpen && (
                    <div className="flex flex-col items-center justify-center flex-grow text-center px-4">
                        <p className="text-red-500 text-lg">Error: {error}</p>
                        <p className="text-gray-600 mt-2">Could not load schedules. Please check the connection and try again.</p>
                        <button onClick={() => fetchSchedules()} className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Retry</button>
                    </div>
                )}

                {!loading && !error && (
                    <div className="overflow-x-auto flex-grow">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Capacity</th>
                                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {schedules.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="px-6 py-4 text-center text-sm text-gray-500">No schedules found. Click 'New Schedule' to add one.</td>
                                    </tr>
                                )}
                                {schedules.map((schedule) => (
                                    <tr key={schedule.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{schedule.name}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                             <div className='flex items-center gap-1'>
                                                <BsClock/> 
                                                {formatTime(schedule.startTime)} - {formatTime(schedule.endTime)}
                                             </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                             <div className='flex items-center gap-1'>
                                                <BsPeople/> 
                                                {schedule.currentEmployees} / {schedule.maxEmployees}
                                             </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                                            <button onClick={() => handleEdit(schedule)} className="text-blue-600 hover:text-blue-900" title="Edit">
                                                <BsPencilSquare />
                                            </button>
                                            <button onClick={() => handleDelete(schedule)} className="text-red-600 hover:text-red-900" title="Delete">
                                                <BsTrash />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                 {/* Create/Edit Schedule Modal */}
                 <Modal 
                    isOpen={isModalOpen} 
                    onClose={handleCloseModal} 
                    title={selectedSchedule ? "Edit Schedule" : "Create New Schedule"}
                    maxWidth="max-w-2xl" // Make modal slightly wider for the form
                 >
                    {/* Pass form error state down if needed, or handle within ScheduleForm */}
                    {/* {error && <p className="text-red-500 mb-4">Error: {error}</p>} */} 
                    <ScheduleForm 
                        initialData={selectedSchedule} 
                        onSubmit={handleFormSubmit}
                        onCancel={handleCloseModal}
                        isSaving={isSaving}
                    />
                 </Modal>

                 {/* Delete Confirmation Modal would go here */}
                 {/* e.g., <ConfirmationModal isOpen={isDeleteConfirmOpen} ... /> */}
            </div>
        </Grid>
    );
};

export default ManageSchedules; 