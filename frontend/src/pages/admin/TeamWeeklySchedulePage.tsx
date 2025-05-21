import React, { useState, useEffect } from 'react';
import { Calendar, momentLocalizer, Event as CalendarEvent } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import useApiPrivate from '../../hooks/useApiPrivate';
import useAuth from '../../hooks/useAuth';
import { Employee } from '../../types/Employee';
import { Schedule } from '../../types/Schedule';

const localizer = momentLocalizer(moment);

interface TeamScheduleEvent extends CalendarEvent {
  id: number; // Changed from string to number to match potential emp.id usage
  title: string;
  start: Date;
  end: Date;
  allDay?: boolean;
  resource?: any; // employeeId or other details
}

const TeamWeeklySchedulePage: React.FC = () => {
  const [events, setEvents] = useState<TeamScheduleEvent[]>([]);
  const [currentManager, setCurrentManager] = useState<Employee | null>(null);
  const [teamEmployees, setTeamEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const apiPrivate = useApiPrivate();
  const { userEmail, isAuthenticated } = useAuth(); // Destructure needed auth properties

  useEffect(() => {
    const fetchManagerAndTeamSchedules = async () => {
      setLoading(true);
      setError(null);
      setEvents([]); // Clear previous events
      setCurrentManager(null);
      setTeamEmployees([]);

      if (!isAuthenticated || !userEmail) {
        setError("User not authenticated or email is missing. Cannot load team schedule.");
        setLoading(false);
        return;
      }

      try {
        const managerResponse = await apiPrivate.get<Employee>(`/api/employees/search?employeeEmail=${userEmail}`);
        const manager = managerResponse.data;
        setCurrentManager(manager);

        if (!manager || !manager.managedTeam || typeof manager.managedTeam.id === 'undefined') {
          setError(manager ? "You do not manage a team. Cannot display team schedule." : "Failed to retrieve your manager details.");
          setLoading(false);
          return;
        }
        const teamId = manager.managedTeam.id;

        const teamEmployeesResponse = await apiPrivate.get<Employee[]>(`/api/employees/byTeam/${teamId}`);
        const fetchedTeamEmployees = teamEmployeesResponse.data || [];
        setTeamEmployees(fetchedTeamEmployees);

        if (fetchedTeamEmployees.length === 0) {
            setLoading(false);
            // No error, but no employees to show schedule for yet.
            return;
        }

        const startOfWeek = moment(currentDate).startOf('week').toDate();
        const teamScheduleEvents: TeamScheduleEvent[] = [];

        for (const emp of fetchedTeamEmployees) {
          if (emp.schedule && emp.schedule.startTime && emp.schedule.endTime) {
            for (let i = 0; i < 7; i++) {
              const dayInWeek = moment(startOfWeek).add(i, 'days');
              try {
                const [startHour, startMinute] = emp.schedule.startTime.split(':').map(Number);
                const [endHour, endMinute] = emp.schedule.endTime.split(':').map(Number);

                const eventStart = moment(dayInWeek).hour(startHour).minute(startMinute).second(0).toDate();
                const eventEnd = moment(dayInWeek).hour(endHour).minute(endMinute).second(0).toDate();

                if (eventEnd > eventStart) {
                  teamScheduleEvents.push({
                    id: emp.id * 1000 + i, 
                    title: `${emp.names} ${emp.firstSurname} - ${emp.schedule.name}`,
                    start: eventStart,
                    end: eventEnd,
                    resource: emp.id,
                  });
                }
              } catch (e) {
                console.warn(`Could not parse schedule times for ${emp.email} (${emp.schedule.name}): ${e}`);
              }
            }
          }
        }
        setEvents(teamScheduleEvents);

      } catch (err: any) {
        console.error("Error fetching team schedule data:", err);
        setError(`Failed to load team schedule: ${err.message || 'Please try again.'}`);
      } finally {
        setLoading(false);
      }
    };

    fetchManagerAndTeamSchedules();
  }, [apiPrivate, userEmail, isAuthenticated, currentDate]);

  const handleNavigate = (newDate: Date) => {
    setCurrentDate(newDate);
  };

  let headerContent;
  if (loading) {
    headerContent = <div className="text-center text-gray-500 py-4">Loading team schedule... <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-blue-500 mx-auto mt-2"></div></div>;
  } else if (error) {
    headerContent = <div className="text-center text-red-600 py-4 bg-red-50 p-3 rounded-md">Error: {error}</div>;
  } else if (!currentManager || !currentManager.managedTeam) {
    headerContent = <div className="text-center text-orange-600 py-4 bg-orange-50 p-3 rounded-md">You are not currently managing a team.</div>;
  } else if (teamEmployees.length === 0) {
    headerContent = <div className="text-center text-gray-500 py-4">No employees found in your managed team ({currentManager.managedTeam.name}).</div>;
  } else {
    headerContent = (
        <div className="flex flex-col sm:flex-row justify-between items-center mb-4">
            <h1 className="text-xl sm:text-2xl font-semibold text-gray-800">
                Weekly Schedule: {currentManager.managedTeam.name}
            </h1>
            <p className="text-sm text-gray-600 mt-1 sm:mt-0">
                Viewing: {moment(currentDate).startOf('week').format('MMM D')} - {moment(currentDate).endOf('week').format('MMM D, YYYY' )}
            </p>
        </div>
    );
  }

  return (
    <div className="p-4 md:p-6 bg-slate-50 min-h-screen">
      <div className="bg-white p-4 sm:p-6 rounded-xl shadow-xl">
        {headerContent}
        
        {!loading && !error && currentManager && currentManager.managedTeam && teamEmployees.length > 0 && (
          <div style={{ height: '650px' }} className="team-schedule-calendar-container mt-4">
            <Calendar
              localizer={localizer}
              events={events}
              startAccessor="start"
              endAccessor="end"
              defaultView="week"
              views={['week', 'day', 'agenda']}
              date={currentDate}
              onNavigate={handleNavigate}
              selectable
              eventPropGetter={(event) => {
                const employee = teamEmployees.find(emp => emp.id === event.resource);
                let backgroundColor = 'bg-blue-500'; // Default
                if (employee) {
                    // Simple hash function for variety, or use a predefined color map by employee id/name
                    const hash = Array.from(employee.names || 'default').reduce((acc, char) => acc + char.charCodeAt(0), 0);
                    const colors = ['bg-sky-500', 'bg-emerald-500', 'bg-amber-500', 'bg-violet-500', 'bg-rose-500', 'bg-cyan-500'];
                    backgroundColor = colors[hash % colors.length];
                }
                return { className: `${backgroundColor} text-white p-0.5 text-xs rounded border-none` , style: { } };
              }}
              style={{ minHeight: 600 }}
              messages={{
                noEventsInRange: "There are no schedules to display for this range."
              }}
              components={{
                event: ({ event }) => (
                  <div title={event.title} className="overflow-hidden whitespace-nowrap text-ellipsis">
                    {event.title}
                  </div>
                )
              }}
            />
          </div>
        )}
        {!loading && !error && events.length === 0 && currentManager && currentManager.managedTeam && teamEmployees.length > 0 && (
          <p className="mt-6 text-center text-gray-500">No schedule entries found for your team for this week.</p>
        )}
      </div>
    </div>
  );
};

export default TeamWeeklySchedulePage; 