import axios from 'axios';

// IMPORTANT: Adjust this URL to match your backend server's address and base path
// Example: If your backend runs on port 8080 and uses /api as base, change to "http://localhost:8080/api"
const BASE_URL = 'http://localhost:8081/api/v1'; // Assuming port 8081 and /api/v1 base path

/**
 * Default Axios instance for public API calls.
 */
const apiClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * Axios instance for private API calls that require authentication.
 */
export const apiPrivate = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true, // Important for sending cookies (like refresh tokens)
});

// --- Axios Interceptor --- 
// Add an interceptor to apiPrivate to attach the Authorization header
apiPrivate.interceptors.request.use(
    (config) => {
        // --- IMPORTANT --- 
        // Get the access token from your authentication context/storage
        // This is a placeholder - Replace with your actual token retrieval logic
        // Example: const token = JSON.parse(localStorage.getItem('authState') || '{}')?.token;
        const token = localStorage.getItem('token'); // Use 'token' key based on user screenshot
        
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// You might add interceptors here later to handle token attachment and refreshing
// apiClient.interceptors.request.use(...)
// apiPrivate.interceptors.response.use(...)


export default apiClient; // Export the default instance primarily 