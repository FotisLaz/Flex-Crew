export interface Team {
    id: number;
    name: string;
}

export interface Employee {
    id: number;
    names: string;
    firstSurname: string;
    // email: string;
    // imageUrl?: string;
    role: string; // Or a more specific Role enum if you have one on the frontend
    team?: Team; // Assuming an employee is directly associated with a team
    // fk_schedule?: number;
    // fk_managed_team?: number;
}

export interface Project {
    projectId: number;
    name: string;
    description?: string;
    status?: string;
    creationDate?: string; // Or Date object, consider conversion
    dueDate?: string;      // Or Date object, consider conversion
    // requiredTeams and employeeAssignments might not be directly part of the Project entity from GET /projects
    // but are available through specific entities or the suggestion DTO.
}

export interface EmployeeSuggestion {
    employeeId: number;
    names: string;
    firstSurname: string;
    teamName: string;
}

export interface RequiredTeamSuggestion {
    teamId: number;
    teamName: string;
    requiredCount: number;
    assignedCount: number;
    neededCount: number;
    assignedEmployees: EmployeeSuggestion[];
    suggestedEmployees: EmployeeSuggestion[];
}

export interface ProjectAssignmentSuggestionDTO {
    projectId: number;
    projectName: string;
    teamSuggestions: RequiredTeamSuggestion[];
}

// For creating/updating projects, you might need a different type
export interface ProjectFormData {
    name: string;
    description?: string;
    status?: string;
    dueDate?: string; // ISO string format for dates
    // You'll also need a way to define required teams when creating/updating a project.
    // This could be an array of objects like: { teamId: number; requiredCount: number; }
    // For simplicity, we'll focus on displaying existing projects and suggestions first.
} 