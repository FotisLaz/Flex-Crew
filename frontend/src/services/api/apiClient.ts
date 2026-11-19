import axios from 'axios';
import { attachRefreshOn401 } from '../../api/axiosConfig';

const BASE_URL = '/api/v1';

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

attachRefreshOn401(apiPrivate);


export default apiClient; // Export the default instance primarily 