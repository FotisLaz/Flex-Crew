import { Navigate, Outlet, RouterProps, useLocation } from "react-router-dom";
import useAuth from "../../hooks/useAuth";

const RequireAuth = () => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  // Temporarily bypass authentication check for testing
  // return isAuthenticated ? (
  //   <Outlet />
  // ) : (
  //   <Navigate to="/authentication" replace={true} state={{ from: location }} />
  // );
  
  // Always allow access for testing the UI
  return <Outlet />;
};

export default RequireAuth;
