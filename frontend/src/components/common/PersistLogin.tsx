import { Outlet } from "react-router-dom";
import { useState, useEffect } from "react";
import { refreshTokens } from "../../api/axiosConfig";
import useAuth from "../../hooks/useAuth";

const PersistLogin = () => {
  const [isLoading, setIsLoading] = useState(true);
  const { isAuthenticated, token } = useAuth();
  // Decided once on mount: re-checking on later auth changes would start a refresh during
  // logout, before the provider has cleared the stored refresh token.
  const [shouldRefreshToken] = useState(
    () => !isAuthenticated && !!localStorage.getItem('refreshToken'),
  );

  useEffect(() => {
    let isMounted = true;

    const verifyRefreshToken = async () => {
      try {
        console.log("Attempting to refresh token...");
        await refreshTokens();
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
  }, [shouldRefreshToken]);

  useEffect(() => {
    console.log(`isLoading: ${isLoading}`);
    console.log(`Access Token: ${token ? 'Yes' : 'No'}`);
    console.log(`isAuthenticated: ${isAuthenticated}`);
  }, [isLoading, token, isAuthenticated]);

  return <>{isLoading ? <p>Loading...</p> : <Outlet />}</>;
};

export default PersistLogin;
