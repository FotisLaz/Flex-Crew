import { apiPrivate } from "../api/axiosConfig";
import { useEffect } from "react";
import useAuth from "./useAuth";

const useApiPrivate = () => {
  const { token } = useAuth();

  useEffect(() => {
    const requestIntercept = apiPrivate.interceptors.request.use(
      (config) => {
        // If its the initial request or after a refresh
        // add token
        if (!config.headers["Authorization"]) {
          config.headers["Authorization"] = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error),
    );

    // Interceptors needs to be attached and then deattached
    return () => {
      apiPrivate.interceptors.request.eject(requestIntercept);
    };
  }, [token]);
  return apiPrivate;
};

export default useApiPrivate;