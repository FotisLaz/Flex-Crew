import { Outlet } from "react-router-dom";
import { useState, useEffect } from "react";
import useRefreshToken from "../../hooks/useRefreshToken";
import useAuth from "../../hooks/useAuth";

const PersistLogin = () => {
  const [isLoading, setIsLoading] = useState(true);
  const refresh = useRefreshToken();
  const { isAuthenticated, token, tokenForRefresh } = useAuth();

  useEffect(() => {
    // Check if we need to attempt refreshing token
    const shouldRefreshToken = !isAuthenticated && !!localStorage.getItem('refreshToken');
    
    let isMounted = true;

    const verifyRefreshToken = async () => {
      try {
        console.log("Attempting to refresh token...");
        await refresh();
      } catch (err) {
        console.error("Failed to refresh token:", err);
      } finally {
        // Only update state if the component is still mounted
        if (isMounted) setIsLoading(false);
      }
    };

    // If we're not authenticated but have a refresh token in localStorage
    if (shouldRefreshToken) {
      verifyRefreshToken();
    } else {
      setIsLoading(false);
    }

    // Cleanup function to handle component unmounting
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    console.log(`isLoading: ${isLoading}`);
    console.log(`Access Token: ${token ? 'Yes' : 'No'}`);
    console.log(`isAuthenticated: ${isAuthenticated}`);
  }, [isLoading, token, isAuthenticated]);

  return <>{isLoading ? <p>Loading...</p> : <Outlet />}</>;
};

export default PersistLogin;
