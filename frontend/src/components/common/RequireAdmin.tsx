import { Navigate, Outlet, useLocation } from "react-router-dom";
import useAuth from "../../hooks/useAuth";

const RequireAdmin = () => {
  const { isAuthenticated, userRole } = useAuth();
  const location = useLocation();

  // Check if authenticated AND if the role is ADMIN
  if (!isAuthenticated) {
    // Redirect them to the login page, but save the current location they were
    // trying to go to when they were redirected. This allows us to send them
    // along to that page after they login, which is a nicer user experience
    // than dropping them off on the home page.
    return <Navigate to="/authentication" state={{ from: location }} replace />;
  }

  if (userRole !== 'ADMIN') {
    // If authenticated but not an admin, redirect to a general page (e.g., dashboard)
    // Or show an "Unauthorized" page
    // Redirecting to dashboard for now
    console.warn("Unauthorized access attempt to admin route by user:", userRole);
    return <Navigate to="/" state={{ from: location }} replace />;
    // Alternatively, create and navigate to an Unauthorized component:
    // return <Navigate to="/unauthorized" state={{ from: location }} replace />;
  }

  // If authenticated and is an ADMIN, render the child component (Outlet)
  return <Outlet />;
};

export default RequireAdmin; 