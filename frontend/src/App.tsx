import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ProjectsListPage from './pages/ProjectsListPage';
import ProjectDetailsPage from './pages/ProjectDetailsPage';
import SettingsPage from "./components/SettingsPage";
import Dashboard from "./components/pages/Dashboard";
import MyTeam from "./components/pages/MyTeam";
import Authentication from "./components/pages/Authentication";
import RequireAuth from "./components/common/RequireAuth";
import Missing from "./components/common/Missing";
import PersistLogin from "./components/common/PersistLogin";
import WeeklySchedule from "./components/pages/WeeklySchedule";
import UserProfile from "./components/pages/UserProfile";
import RequireAdmin from "./components/common/RequireAdmin";
import ManageSchedules from "./components/pages/admin/ManageSchedules";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import TeamWeeklySchedulePage from "./pages/admin/TeamWeeklySchedulePage";

function App() {
  return (
    <Router>
      <Routes>
        {/* Public routes */}
        <Route path="/authentication" element={<Authentication />} />
        
        {/* Protected routes */}
        <Route element={<PersistLogin />}>
          <Route element={<RequireAuth />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/my-team" element={<MyTeam />} />
            <Route path="/my-schedule" element={<WeeklySchedule />} />
            <Route path="/profile" element={<UserProfile />} />
            <Route path="/settings" element={<SettingsPage />} />
            
            <Route path="/projects" element={<ProjectsListPage />} />
            <Route path="/projects/:projectId" element={<ProjectDetailsPage />} />

            <Route element={<RequireAdmin />}>
              <Route path="/admin/schedules" element={<ManageSchedules />} />
              <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
              <Route path="/admin/team-weekly-schedule" element={<TeamWeeklySchedulePage />} />
            </Route>
          </Route>
        </Route>

        {/* Catch all */}
        <Route path="/*" element={<Missing />} />
      </Routes>
    </Router>
  );
}

export default App;