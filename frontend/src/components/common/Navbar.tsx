import { useEffect, useState } from "react";
import Card from "./Card";
import { useLocation, useNavigate } from "react-router-dom";
import useAuth from "../../hooks/useAuth";
import { BsHouseDoor, BsPeople, BsGear, BsBoxArrowRight, BsCalendarWeek, BsListTask, BsGraphUp, BsBriefcase } from "react-icons/bs";

const Navbar = () => {
  const { logout, userRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [activeLink, setActiveLink] = useState("");

  const isAdmin = userRole === 'ADMIN';

  const dashBoardPath = "/";
  const myTeamPath = "/my-team";
  const mySchedulePath = "/my-schedule";
  const manageSchedulesPath = "/admin/schedules";
  const adminDashboardPath = "/admin/dashboard";
  const projectsPath = "/projects";
  const settingsPath = "/settings";
  const authenticationPath = "/authentication";

  useEffect(() => {
    const pathname = location.pathname;
    if (pathname === dashBoardPath) {
      setActiveLink("home");
    } else if (pathname === myTeamPath) {
      setActiveLink("myTeam");
    } else if (pathname === mySchedulePath) {
      setActiveLink("mySchedule");
    } else if (pathname === manageSchedulesPath) {
      setActiveLink("manageSchedules");
    } else if (pathname === adminDashboardPath) {
      setActiveLink("adminDashboard");
    } else if (pathname.startsWith(projectsPath)) {
      setActiveLink("projects");
    } else if (pathname === settingsPath) {
      setActiveLink("settings");
    } else {
      setActiveLink("");
    }
  }, [location.pathname]);

  const navigateToDashboard = () => {
    navigate(dashBoardPath);
  };

  const navigateToMyTeam = () => {
    navigate(myTeamPath);
  };
  
  const navigateToMySchedule = () => {
    navigate(mySchedulePath);
  };

  const navigateToManageSchedules = () => {
    navigate(manageSchedulesPath);
  };

  const navigateToAdminDashboard = () => {
    navigate(adminDashboardPath);
  };

  const navigateToProjects = () => {
    navigate(projectsPath);
  };

  const navigateToSettings = () => {
    navigate(settingsPath);
  };

  const navigateToAuth = () => {
    navigate(authenticationPath);
  };

  const handleLogout = () => {
    logout();
    navigateToAuth();
  };

  return (
    <Card color="black-stone flex flex-col h-full">
      <div className="flex flex-col gap-16 flex-grow">
        <h1 className="flex flex-col text-2xl text-white font-bold">
          Flex
          <span className="text-yellow-FlexCrew"> Crew</span>
        </h1>

        <ul className="flex flex-col gap-9 text-sm">
          <li
            onClick={navigateToDashboard}
            className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
              activeLink === "home" ? "text-white" : "text-white/70"
            }`}
          >
            <BsHouseDoor className="text-xl" />
            Dashboard
          </li>
          <li
            onClick={navigateToMyTeam}
            className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
              activeLink === "myTeam" ? "text-white" : "text-white/70"
            }`}
          >
            <BsPeople className="text-xl" />
            My Team
          </li>
          <li
            onClick={navigateToMySchedule}
            className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
              activeLink === "mySchedule" ? "text-white" : "text-white/70"
            }`}
          >
            <BsCalendarWeek className="text-xl" />
            My Schedule
          </li>
          {isAdmin && (
            <>
              <li className="mt-4 pt-4 border-t border-gray-700">
                 <span className="text-xs text-gray-500 uppercase font-semibold">Admin Panel</span>
              </li>
              <li
                onClick={navigateToManageSchedules}
                className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
                  activeLink === "manageSchedules" ? "text-white" : "text-white/70"
                }`}
              >
                <BsListTask className="text-xl" /> 
                Manage Schedules
              </li>
              <li
                onClick={navigateToAdminDashboard}
                className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
                  activeLink === "adminDashboard" ? "text-white" : "text-white/70"
                }`}
              >
                <BsGraphUp className="text-xl" /> 
                Analytics Dashboard
              </li>
              <li
                onClick={navigateToProjects}
                className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
                  activeLink === "projects" ? "text-white" : "text-white/70"
                }`}
              >
                <BsBriefcase className="text-xl" /> 
                Projects
              </li>
            </>
          )}
          <li
            onClick={navigateToSettings}
            className={`flex gap-3 items-center group cursor-pointer hover:text-yellow-FlexCrew transition-colors ${
              activeLink === "settings" ? "text-white" : "text-white/70"
            }`}
          >
            <BsGear className="text-xl" />
            Settings
          </li>
        </ul>
      </div>

      <div 
        onClick={handleLogout}
        className="flex gap-3 items-center text-white/70 text-sm group cursor-pointer hover:text-yellow-FlexCrew transition-colors pt-2 pb-4 border-t border-gray-700"
      >
        <BsBoxArrowRight className="text-xl" />
        Logout
      </div>
    </Card>
  );
};

export default Navbar;
