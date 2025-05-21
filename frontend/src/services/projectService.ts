import { Project, ProjectAssignmentSuggestionDTO, ProjectFormData } from '../types/projectTypes';
import apiPrivate from './api/apiClient'; 

// Ensure that VITE_API_BASE_URL is treated as unset if it's an empty string, then fallback to /api/v1
// const envApiBaseUrl = import.meta.env.VITE_API_BASE_URL;
// const API_BASE_URL = envApiBaseUrl && envApiBaseUrl.trim() !== '' ? envApiBaseUrl : '/api/v1';
// API_BASE_URL will be handled by the apiClient

// Helper function for handling API responses (adapted for Axios)
async function handleResponse<T>(response: { data: T; status: number }): Promise<T> {

    if (response.status === 204) {
        return null as T;
    }
    return response.data;
}

export const projectService = {
    async getAllProjects(): Promise<Project[]> {
        const urlToFetch = '/projects'; // Relative to baseURL in apiClient
        console.log('Fetching projects from URL (using apiPrivate):', urlToFetch);
        const response = await apiPrivate.get(urlToFetch);
        return handleResponse<Project[]>(response);
    },

    async getProjectById(projectId: number): Promise<Project> {
        const response = await apiPrivate.get(`/projects/${projectId}`);
        return handleResponse<Project>(response);
    },

    async createProject(projectData: ProjectFormData): Promise<Project> {
        const response = await apiPrivate.post('/projects', projectData);
        return handleResponse<Project>(response);
    },

    async updateProject(projectId: number, projectData: Partial<ProjectFormData>): Promise<Project> {
        const response = await apiPrivate.put(`/projects/${projectId}`, projectData);
        return handleResponse<Project>(response);
    },

    async deleteProject(projectId: number): Promise<void> {
        const response = await apiPrivate.delete(`/projects/${projectId}`);
        // For delete, we might not always get data back, handle appropriately
        if (response.status === 204 || response.status === 200) { // 200 if it returns some confirmation
             return null as unknown as void; // Or just return;
        }
        // Axios will throw for other statuses, this is more for type compatibility if data is expected
        return handleResponse<void>(response);
    },

    async getAssignmentSuggestions(projectId: number): Promise<ProjectAssignmentSuggestionDTO> {
        const response = await apiPrivate.get(`/projects/${projectId}/assignment-suggestions`);
        return handleResponse<ProjectAssignmentSuggestionDTO>(response);
    },
    

}; 