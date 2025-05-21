import useAuth from "./useAuth";
import api from "../api/axiosConfig";

const useRefreshToken = () => {
  const { setRefreshToken, tokenForRefresh } = useAuth();

  const refresh = async () => {
    // Get refresh token from context or localStorage as fallback
    const refreshTokenToUse = tokenForRefresh || localStorage.getItem('refreshToken');
    
    // If no refresh token is available, throw an error
    if (!refreshTokenToUse) {
      throw new Error("No refresh token available");
    }
    
    try {
      const response = await api.post(
        "/api/auth/refresh-token",
        {},
        {
          headers: {
            Authorization: `Bearer ${refreshTokenToUse}`,
          },
        },
      );

      if (response.data?.access_token) {
        console.log("Successfully refreshed access token");
        setRefreshToken(response.data.access_token);
        return response.data.access_token;
      } else {
        throw new Error("No access token received from server");
      }
    } catch (error) {
      console.error("Error refreshing token:", error);
      // Clear localStorage on refresh failure
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      throw error;
    }
  };

  return refresh;
};

export default useRefreshToken; 