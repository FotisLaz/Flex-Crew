import React, { useEffect, useState } from 'react';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import axios from 'axios';
import { Schedule } from '../../types/Schedule';

const localizer = momentLocalizer(moment);

interface CalendarEvent {
  title: string;
  start: Date;
  end: Date;
  allDay?: boolean;
}

const WeeklySchedule: React.FC = () => {
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSchedule = async () => {
      try {
        setLoading(true);
        const response = await axios.get<Schedule[]>('/api/schedules/employee/current');
        
        // Process the schedule data into calendar events
        const calendarEvents = processScheduleData(response.data);
        setEvents(calendarEvents);
        setError(null);
      } catch (err) {
        console.error('Error fetching schedule:', err);
        setError('Failed to load your schedule. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchSchedule();
  }, []);

  const processScheduleData = (schedules: Schedule[]): CalendarEvent[] => {
    if (!schedules || schedules.length === 0) {
      return [];
    }

    // Get next week's dates (Monday to Friday)
    const today = new Date();
    const nextMonday = getNextMonday(today);
    
    return schedules.map((schedule, index) => {
      // Calculate the day for this schedule (index 0 = Monday, 1 = Tuesday, etc.)
      const day = new Date(nextMonday);
      day.setDate(day.getDate() + index);
      
      // Parse the start and end times and set them on the correct day
      const startDateTime = createDateTimeFromTimeString(day, schedule.startTime);
      const endDateTime = createDateTimeFromTimeString(day, schedule.endTime);
      
      return {
        title: schedule.name,
        start: startDateTime,
        end: endDateTime,
        allDay: false
      };
    });
  };

  // Helper function to get next Monday from a given date
  const getNextMonday = (date: Date): Date => {
    const dateCopy = new Date(date);
    const dayOfWeek = dateCopy.getDay(); // 0 = Sunday, 1 = Monday, etc.
    const daysUntilNextMonday = dayOfWeek === 0 ? 1 : 8 - dayOfWeek;
    dateCopy.setDate(dateCopy.getDate() + daysUntilNextMonday);
    return dateCopy;
  };

  // Helper function to create a Date object from a time string (e.g., "09:00:00")
  const createDateTimeFromTimeString = (day: Date, timeString: string): Date => {
    const [hours, minutes] = timeString.split(':').map(Number);
    const result = new Date(day);
    result.setHours(hours, minutes, 0, 0);
    return result;
  };

  if (loading) {
    return <div className="flex justify-center items-center h-screen">Loading your schedule...</div>;
  }

  if (error) {
    return <div className="flex justify-center items-center h-screen text-red-500">{error}</div>;
  }

  return (
    <div className="h-screen p-6">
      <h1 className="text-2xl font-bold mb-6">My Weekly Schedule</h1>
      <div className="h-5/6">
        <Calendar
          localizer={localizer}
          events={events}
          startAccessor="start"
          endAccessor="end"
          defaultView="week"
          views={['week', 'day']}
          min={new Date(0, 0, 0, 7, 0, 0)}  // 7:00 AM
          max={new Date(0, 0, 0, 19, 0, 0)} // 7:00 PM
          className="bg-white rounded-lg shadow p-4"
        />
      </div>
    </div>
  );
};

export default WeeklySchedule; 