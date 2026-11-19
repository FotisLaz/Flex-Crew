import { ReactNode, createContext, useCallback, useEffect, useState } from "react";
import api, { onTokensChanged } from "../api/axiosConfig";

// Define the shape of the context data
interface AuthContextProps {
  isAuthenticated: boolean;
  token: string | null;
  tokenForRefresh: string | null;
  userEmail: string | null;
  userRole: string | null;
  login: (token: string, refreshToken: string, userEmail: string, userRole: string) => void;
  logout: () => void;
}

// Create the context with an initial undefined value
const AuthContext = createContext<AuthContextProps | undefined>(undefined);

// Define props for the provider component
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  // Initialize states from localStorage if available
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [tokenForRefresh, setTokenForRefresh] = useState<string | null>(() => localStorage.getItem('refreshToken'));
  const [userEmail, setUserEmail] = useState<string | null>(() => localStorage.getItem('userEmail'));
  const [userRole, setUserRole] = useState<string | null>(() => localStorage.getItem('userRole'));

  // Effect to update localStorage when auth state changes
  useEffect(() => {
    if (token && userEmail && userRole && tokenForRefresh) {
      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', tokenForRefresh);
      localStorage.setItem('userEmail', userEmail);
      localStorage.setItem('userRole', userRole);
    } else {
      // Clear localStorage on logout or if essential data is missing
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userRole');
    }
  }, [token, tokenForRefresh, userEmail, userRole]);

  // Login function: Stores tokens, email, and role
  const login = (newToken: string, refreshToken: string, email: string, role: string) => {
    setToken(newToken);
    setTokenForRefresh(refreshToken);
    setUserEmail(email);
    setUserRole(role);
    // LocalStorage is updated by the useEffect hook
  };

  // Logout function: Clears state and localStorage
  const logout = useCallback(() => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      // Best effort: the local session ends either way, and a token the server could not revoke
      // still expires.
      api
        .post('/api/auth/logout', {}, { headers: { Authorization: `Bearer ${refreshToken}` } })
        .catch(() => undefined);
    }
    setToken(null);
    setTokenForRefresh(null);
    setUserEmail(null);
    setUserRole(null);
    // LocalStorage is cleared by the useEffect hook
  }, []);

  // Keep state in sync with token refreshes done by the axios interceptor
  useEffect(
    () =>
      onTokensChanged((tokens) => {
        if (tokens) {
          setToken(tokens.accessToken);
          setTokenForRefresh(tokens.refreshToken);
        } else {
          logout();
        }
      }),
    [logout],
  );

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated: token !== null,
        token,
        tokenForRefresh,
        userEmail,
        userRole,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
