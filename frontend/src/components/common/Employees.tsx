import { useState, useEffect } from "react";
import { Employee } from "../../types/Employee";
import { Team } from "../../types/Team";
import useApiPrivate from "../../hooks/useApiPrivate";
import { useNavigate, useLocation } from "react-router-dom";
import logo from "../../assets/images/logo.webp";
import { BsSearch, BsPerson, BsPeople } from "react-icons/bs";

interface EmployeesProps {
  searchFilter?: string;
}

export const Employees = ({ searchFilter = '' }: EmployeesProps) => {
  const [employees, setEmployees] = useState<Employee[] | null>();
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const apiPrivate = useApiPrivate();
  const navigate = useNavigate();
  const location = useLocation();

  // only run in component load, empty dependency array
  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();

    const getEmployees = async () => {
      try {
        setLoading(true);
        // Uses the tokens expiration proof, aka apiPrivate, to get employees
        const response = await apiPrivate.get("/api/employees", {
          signal: controller.signal,
        });
        
        if (isMounted) {
          const employeeData = response.data;
          setEmployees(employeeData);
          
          // Extract unique teams from employees
          const uniqueTeams = Array.from(
            new Set(
              employeeData
                .filter((emp: Employee) => emp.team)
                .map((emp: Employee) => JSON.stringify(emp.team))
            )
          ).map(teamStr => JSON.parse(teamStr));
          
          setTeams(uniqueTeams);
          setError(null);
        }
      } catch (err) {
        // if the refresh token expires
        // go to authentication and save the route to return back where you where
        console.error(err);
        if (isMounted) {
          setError("Failed to fetch employees. Please try again.");
        }
        navigate("/authentication", {
          state: { from: location },
          replace: true,
        });
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    getEmployees();

    // Clean up function
    return () => {
      isMounted = false;
      controller.abort();
    };
  }, []);

  // Get full name of employee
  const getFullName = (employee: Employee) => {
    return `${employee.names} ${employee.firstSurname} ${employee.secondSurname}`.trim();
  };

  // Filter employees based on search term
  const filteredEmployees = employees?.filter(employee => {
    if (!searchFilter) return true;
    
    const fullName = getFullName(employee).toLowerCase();
    const email = employee.email.toLowerCase();
    const searchLower = searchFilter.toLowerCase();
    
    return fullName.includes(searchLower) || email.includes(searchLower);
  });

  // Get teams from filtered employees
  const filteredTeams = teams.filter(team => {
    if (!filteredEmployees) return false;
    return filteredEmployees.some(employee => employee.team?.id === team.id);
  });

  // Group employees by team
  const getEmployeesByTeam = (teamId: number) => {
    return filteredEmployees?.filter(employee => employee.team?.id === teamId) || [];
  };

  // Get role badge style based on role
  const getRoleBadge = (employee: Employee) => {
    const isManager = employee.managedTeam !== null;
    return isManager 
      ? "bg-yellow-FlexCrew text-black" 
      : "bg-blue-100 text-blue-800";
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-yellow-FlexCrew"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-screen">
        <p className="text-red-500 text-xl mb-4">{error}</p>
        <button 
          onClick={() => window.location.reload()} 
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-8">
      {/* Teams and Members section */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold mb-6 text-gray-800 border-b pb-3">
          Employees by Teams
          {searchFilter && (
            <span className="ml-2 text-sm font-normal text-gray-500">
              Filtering by: "{searchFilter}"
            </span>
          )}
        </h2>
        
        {filteredTeams.length > 0 ? (
          <div className="space-y-8">
            {filteredTeams.map((team) => (
              <div key={team.id} className="border-l-4 border-yellow-FlexCrew pl-4">
                <h3 className="text-lg font-semibold mb-4 flex items-center">
                  <BsPeople className="mr-2 text-yellow-FlexCrew" size={20} />
                  {team.name}
                </h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {getEmployeesByTeam(team.id).map((employee) => (
                    <div 
                      key={employee.id} 
                      className="bg-gray-50 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow"
                    >
                      <div className="flex items-center space-x-4">
                        <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden">
                          {employee.imageUrl ? (
                            <img 
                              src={employee.imageUrl} 
                              alt={getFullName(employee)} 
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <BsPerson className="text-2xl text-gray-400" />
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-gray-900 truncate">
                            {getFullName(employee)}
                          </p>
                          <p className="text-sm text-gray-500 truncate">
                            {employee.email}
                          </p>
                          <div className="mt-1">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getRoleBadge(employee)}`}>
                              {employee.managedTeam ? 'Manager' : 'Member'}
                            </span>
                            {employee.schedule && (
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 ml-2">
                                {employee.schedule.name}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <BsSearch className="mx-auto text-4xl text-gray-300 mb-3" />
            <p className="text-gray-500">
              {searchFilter 
                ? `No employees found matching "${searchFilter}"`
                : "No teams or employees found"}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Employees;
