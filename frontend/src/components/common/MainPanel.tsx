// import logo from "../../assets/images/logo.webp"; // Logo moved or unused?
import { useEffect, useState, useMemo } from "react";
import moment from 'moment'; // Added import for moment
import { Employee } from "../../types/Employee";
import { Issue } from "../../types/Issue"; // Assuming an Issue type exists
import useAuth from "../../hooks/useAuth";
import useApiPrivate from "../../hooks/useApiPrivate";
import { Link, useNavigate } from "react-router-dom";
import { BsCalendar, BsCupHot, BsChevronLeft, BsChevronRight, BsExclamationTriangle, BsPeople, BsPersonCheck } from "react-icons/bs"; // Removed BsBell, Added BsPeople, BsPersonCheck
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { FaUsers, FaUserCheck, FaExclamationTriangle as FaExclamationTriangleIcon, FaCalendarAlt, FaCoffee, FaCalendarWeek, FaChevronLeft, FaChevronRight } from 'react-icons/fa';
import Card from './Card'; // Assuming default export for Card

// Helper function to count employees per team
const countEmployeesByTeam = (employees: Employee[]) => {
    const counts: { [key: string]: number } = {};
    employees.forEach(emp => {
        const teamName = emp.team?.name || 'No Team';
        counts[teamName] = (counts[teamName] || 0) + 1;
    });
    return Object.entries(counts).map(([name, count]) => ({ name, count }));
};

// Updated color scheme using theme colors (yellows/oranges)
const CHART_COLORS = ['#EBB200', '#F59E0B', '#D97706', '#FBBF24', '#FCD34D', '#FEF3C7']; // yellow-FlexCrew and shades

const MainPanel = () => {
  const [currentEmployee, setCurrentEmployee] = useState<Employee | null>(null);
  const [allEmployees, setAllEmployees] = useState<Employee[]>([]);
  const [issues, setIssues] = useState<Issue[]>([]);
  const [currentDateState, setCurrentDateState] = useState(new Date(2025, 4, 1)); // May 2025
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const authContext = useAuth(); // Get the whole auth context
  const apiPrivate = useApiPrivate();

  useEffect(() => {
    const fetchInitialData = async () => {
      setLoading(true);
      setError(null);
      setCurrentEmployee(null); // Reset current employee before fetching

      if (!authContext.isAuthenticated || !authContext.userEmail) {
        setLoading(false);
        setError("User not authenticated or email not available.");
        console.warn("Dashboard: User not authenticated or email not available for fetching data.");
          return;
        }
        
      try {
        const employeesResponse = await apiPrivate.get("/api/employees");
        const fetchedAllEmployees = employeesResponse.data || [];
        setAllEmployees(fetchedAllEmployees);

        const issuesResponse = await apiPrivate.get("/api/issues");
        setIssues(issuesResponse.data || []);

        // Fetch current employee data using email from context
        // This assumes /api/employees/search?employeeEmail=... returns a single Employee object or null/404
        const empByEmailResponse = await apiPrivate.get(`/api/employees/search?employeeEmail=${authContext.userEmail}`);
        setCurrentEmployee(empByEmailResponse.data); // If not found, API should ideally return 404, and data would be null/undefined

        if (!empByEmailResponse.data) {
            console.warn(`Dashboard: Employee with email ${authContext.userEmail} not found via search endpoint.`);
            // Optionally, try to find from the allEmployees list as a fallback, though less ideal if search endpoint is primary
            const foundInAll = fetchedAllEmployees.find((emp: Employee) => emp.email === authContext.userEmail);
            if (foundInAll) setCurrentEmployee(foundInAll);
            else setError("Could not retrieve your employee details."); // If not found anywhere
        }

      } catch (err: any) {
        console.error("Error fetching initial dashboard data:", err);
        setError(`Failed to load dashboard data: ${err.message || 'Please try again.'}`);
      } finally {
        setLoading(false);
      }
    };

    fetchInitialData();
  }, [apiPrivate, authContext.userEmail, authContext.isAuthenticated]); // Added isAuthenticated to dependencies

  const managedEmployeesCount = useMemo(() => {
    if (currentEmployee && currentEmployee.managedTeam && allEmployees.length > 0) {
      return allEmployees.filter(emp => emp.team?.id === currentEmployee.managedTeam?.id).length;
    }
    // If not a manager or relevant data missing, this might show total or 0 based on requirements
    return currentEmployee?.managedTeam ? 0 : allEmployees.length; 
  }, [currentEmployee, allEmployees]);

  const openIssuesCount = useMemo(() => {
    // Placeholder: filter issues relevant to the manager's team or context
    // For now, it counts all issues. This should be refined.
    return issues.length;
  }, [issues]);

  const handleViewWeeklySchedule = () => {
    navigate('/admin/team-weekly-schedule');
  };

  const getMonthName = () => currentDateState.toLocaleString('en-US', { month: 'long' });
  const getDaysInMonth = () => {
    const days = [];
    const lastDayOfMonth = new Date(currentDateState.getFullYear(), currentDateState.getMonth() + 1, 0);
    for (let i = 1; i <= lastDayOfMonth.getDate(); i++) days.push(i);
    return days;
  };
  const getWeekdayNames = () => ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  // Calculate data for the chart
  const employeesPerTeamData = countEmployeesByTeam(allEmployees);

  if (loading) return <div className="flex justify-center items-center h-screen"><div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div><span className="ml-3 text-gray-600">Loading...</span></div>;
  if (error && !currentEmployee) return <div className="p-6 text-center text-red-500 text-lg">Error: {error} <button onClick={() => window.location.reload()} className="ml-2 px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">Retry</button></div>;

  return (
    <div className="p-4 sm:p-6 bg-slate-100 min-h-screen">
      {error && <div className="mb-4 p-3 bg-red-100 text-red-700 border border-red-300 rounded-md text-center">Data loading error: {error}</div>} 
      <div className="mb-6 p-4 bg-white shadow-lg rounded-xl">
        <h2 className="text-2xl md:text-3xl font-bold text-gray-800">Welcome back, {currentEmployee?.names || authContext.userEmail}!</h2>
        <p className="text-gray-600 mt-1">Here's an overview of your team's current status.</p>
        </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6 mb-6">
        <InfoCard
          icon={<FaUsers className="text-blue-600" size={28} />}
          title="Managed Employees"
          value={currentEmployee ? managedEmployeesCount.toString() : 'N/A'}
          bgColor="bg-blue-50"
          textColor="text-blue-800"
          borderColor="border-blue-200"
        />
        <InfoCard
          icon={<FaUserCheck className="text-green-600" size={28} />}
          title="Active Now"
          value="--" // Placeholder
          bgColor="bg-green-50"
          textColor="text-green-800"
          borderColor="border-green-200"
        />
        <InfoCard
          icon={<FaExclamationTriangleIcon className="text-red-600" size={28} />}
          title="Pending Issues"
          value={openIssuesCount.toString()}
          bgColor="bg-red-50"
          textColor="text-red-800"
          borderColor="border-red-200"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6 mb-6">
        <div className="bg-white p-4 rounded-xl shadow-lg flex items-center space-x-4 border-l-4 border-purple-500">
          <FaCalendarAlt size={32} className="text-purple-600" />
          <div>
            <p className="text-sm text-gray-500">My Current Schedule</p>
            <p className="text-lg font-semibold text-gray-800">{currentEmployee?.schedule?.name || (currentEmployee ? "Not Assigned" : "N/A")}</p>
            {currentEmployee?.schedule && <p className="text-xs text-gray-400">{currentEmployee.schedule.startTime} - {currentEmployee.schedule.endTime}</p>}
          </div>
        </div>
        <div className="bg-white p-4 rounded-xl shadow-lg flex items-center space-x-4 border-l-4 border-orange-500">
          <FaCoffee size={32} className="text-orange-600" />
          <div>
            <p className="text-sm text-gray-500">Next Break Time</p>
            <p className="text-lg font-semibold text-gray-800">14:30 - 15:30</p> {/* Placeholder */}
          </div>
        </div>
        <div 
          onClick={handleViewWeeklySchedule}
          className="bg-indigo-600 text-white p-4 rounded-xl shadow-lg hover:bg-indigo-700 transition-all duration-300 ease-in-out cursor-pointer flex flex-col items-center justify-center text-center transform hover:scale-105"
        >
          <FaCalendarWeek size={32} className="mb-2" />
          <h3 className="text-lg font-semibold">View Weekly Schedule</h3>
          <p className="text-sm opacity-90">Check team availability</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-4 md:gap-6">
        <div className="lg:col-span-3 bg-white p-4 sm:p-6 rounded-xl shadow-lg">
          <h3 className="text-lg font-semibold mb-4 text-gray-700">Employees per Team</h3>
          {allEmployees.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={employeesPerTeamData} margin={{ top: 5, right: 20, left: -20, bottom: 50 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e0e0e0" />
                <XAxis 
                  dataKey="name" 
                  tick={{ fontSize: 10, fill: '#4A5568' }} 
                  interval={0} 
                  angle={-25}
                  textAnchor="end" 
                  height={60}
                />
                <YAxis allowDecimals={false} tick={{ fontSize: 10, fill: '#4A5568' }} width={40} />
                <Tooltip 
                  cursor={{fill: 'rgba(237, 137, 54, 0.1)'}} 
                  contentStyle={{fontSize: '12px', padding: '8px', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)'}} 
                />
                <Bar dataKey="count" name="Employees" radius={[6, 6, 0, 0]}>
                   {employeesPerTeamData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                   ))}
                </Bar> 
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-center text-gray-500 py-12">No team data available for chart. {(currentEmployee && !loading) ? '' : 'Waiting for employee data...'}</p>
          )}
        </div>

        <div className="lg:col-span-2 bg-white rounded-xl shadow-lg overflow-hidden">
          <div className="flex justify-between items-center p-4 bg-gray-50 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-700">{getMonthName()} {currentDateState.getFullYear()}</h3>
            <div className="flex gap-2">
              <button 
                onClick={() => setCurrentDateState(prev => moment(prev).subtract(1, 'month').toDate())}
                className="p-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors shadow-sm"
                aria-label="Previous month"
              >
                <FaChevronLeft />
              </button>
              <button 
                onClick={() => setCurrentDateState(prev => moment(prev).add(1, 'month').toDate())}
                className="p-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors shadow-sm"
                aria-label="Next month"
              >
                <FaChevronRight />
              </button>
            </div>
          </div>
          
          <div className="p-3 sm:p-4">
            <div className="grid grid-cols-7 gap-1 mb-2 text-center text-xs font-medium text-gray-500">
              {getWeekdayNames().map((day) => <div key={day}>{day}</div>)}
            </div>
            
            <div className="grid grid-cols-7 gap-1">
              {/* Placeholder for empty days at the start of the month */}
              {Array.from({ length: moment(currentDateState).startOf('month').day() }).map((_, i) => (
                <div key={`empty-start-${i}`} className="h-8 w-full"></div>
              ))}
              {getDaysInMonth().map((day) => {
                const dayDate = moment(currentDateState).date(day);
                const isToday = moment().isSame(dayDate, 'day');
                return (
                  <div 
                    key={day}
                    className={`h-8 rounded-full flex items-center justify-center cursor-pointer text-xs sm:text-sm transition-all duration-200 ${ 
                      isToday 
                        ? 'bg-yellow-400 text-black font-bold shadow-md' 
                        : 'hover:bg-gray-200'
                    }`}
                    onClick={() => console.log(`Selected day: ${day}`)}
                  >
                    {day}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

interface InfoCardProps {
  icon: JSX.Element;
  title: string;
  value: string;
  bgColor: string;
  textColor: string;
  borderColor: string;
}

const InfoCard: React.FC<InfoCardProps> = ({ icon, title, value, bgColor, textColor, borderColor }) => (
  <div className={`p-4 sm:p-5 rounded-xl shadow-lg flex items-center space-x-4 ${bgColor} border-l-4 ${borderColor}`}>
    <div className="p-2 sm:p-3 bg-white rounded-full shadow-sm">
      {icon}
    </div>
    <div>
      <p className={`text-sm font-medium ${textColor} opacity-90`}>{title}</p>
      <p className={`text-2xl sm:text-3xl font-bold ${textColor}`}>{value}</p>
    </div>
  </div>
);

export default MainPanel;
