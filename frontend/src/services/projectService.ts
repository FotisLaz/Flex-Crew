import { AxiosInstance } from 'axios';
import { Project, ProjectAssignmentSuggestionDTO, ProjectFormData } from '../types/projectTypes';

// Helper function for handling API responses (adapted for Axios)
async function handleResponse<T>(response: { data: T; status: number }): Promise<T> {

    if (response.status === 204) {
        return null as T;
    }
    return response.data;
}

// Callers pass the client from useApiPrivate(), which attaches the access token.
export const projectService = {
    async getAllProjects(api: AxiosInstance): Promise<Project[]> {
        const response = await api.get('/api/v1/projects');
        return handleResponse<Project[]>(response);
    },

    async getProjectById(api: AxiosInstance, projectId: number): Promise<Project> {
        const response = await api.get(`/api/v1/projects/${projectId}`);
        return handleResponse<Project>(response);
    },

    async createProject(api: AxiosInstance, projectData: ProjectFormData): Promise<Project> {
        const response = await api.post('/api/v1/projects', projectData);
        return handleResponse<Project>(response);
    },

    async updateProject(api: AxiosInstance, projectId: number, projectData: Partial<ProjectFormData>): Promise<Project> {
        const response = await api.put(`/api/v1/projects/${projectId}`, projectData);
        return handleResponse<Project>(response);
    },

    async deleteProject(api: AxiosInstance, projectId: number): Promise<void> {
        const response = await api.delete(`/api/v1/projects/${projectId}`);
        // For delete, we might not always get data back, handle appropriately
        if (response.status === 204 || response.status === 200) { // 200 if it returns some confirmation
             return null as unknown as void; // Or just return;
        }
        // Axios will throw for other statuses, this is more for type compatibility if data is expected
        return handleResponse<void>(response);
    },

    async getAssignmentSuggestions(api: AxiosInstance, projectId: number): Promise<ProjectAssignmentSuggestionDTO> {
        const response = await api.get(`/api/v1/projects/${projectId}/assignment-suggestions`);
        return handleResponse<ProjectAssignmentSuggestionDTO>(response);
    },


};