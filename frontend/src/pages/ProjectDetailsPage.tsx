import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { projectService } from '../services/projectService';
import {
    Project,
    ProjectAssignmentSuggestionDTO,
    RequiredTeamSuggestion,
    EmployeeSuggestion
} from '../types/projectTypes';

const ProjectDetailsPage: React.FC = () => {
    const { projectId } = useParams<{ projectId: string }>();
    const [project, setProject] = useState<Project | null>(null);
    const [suggestions, setSuggestions] = useState<ProjectAssignmentSuggestionDTO | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!projectId) {
            setError('Project ID is missing');
            setLoading(false);
            return;
        }

        const fetchProjectData = async () => {
            try {
                setLoading(true);
                const id = parseInt(projectId, 10);
                if (isNaN(id)) {
                    setError('Invalid Project ID format');
                    setLoading(false);
                    return;
                }
                const projectData = await projectService.getProjectById(id);
                setProject(projectData);
                const suggestionData = await projectService.getAssignmentSuggestions(id);
                setSuggestions(suggestionData);
                setError(null);
            } catch (err: any) {
                setError(err.message || 'Failed to fetch project details');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchProjectData();
    }, [projectId]);

    if (loading) return <p>Loading project details...</p>;
    if (error) return <p style={{ color: 'red' }}>Error: {error}</p>;
    if (!project || !suggestions) return <p>Project data not found.</p>;

    const renderEmployeeList = (employees: EmployeeSuggestion[], listTitle: string) => (
        <div>
            <h4>{listTitle} ({employees.length})</h4>
            {employees.length > 0 ? (
                <ul style={{ listStyleType: 'none', paddingLeft: '10px' }}>
                    {employees.map(emp => (
                        <li key={emp.employeeId} style={{ marginBottom: '5px', padding: '5px', background: '#f9f9f9', borderRadius: '3px'}}>
                            {emp.names} {emp.firstSurname} ({emp.teamName})
                        </li>
                    ))}
                </ul>
            ) : <p style={{fontSize: '0.9em', color: 'grey'}}>None</p>}
        </div>
    );

    const renderTeamSuggestion = (teamSuggestion: RequiredTeamSuggestion) => (
        <div key={teamSuggestion.teamId} style={{ border: '1px solid #e0e0e0', borderRadius: '5px', padding: '15px', marginBottom: '15px' }}>
            <h3 style={{ marginTop: 0, color: '#1a237e' }}>{teamSuggestion.teamName}</h3>
            <p>
                <strong>Required:</strong> {teamSuggestion.requiredCount} | 
                <strong>Assigned:</strong> {teamSuggestion.assignedCount} | 
                <strong>Needed:</strong> <span style={{fontWeight: 'bold', color: teamSuggestion.neededCount > 0 ? '#d32f2f' : '#2e7d32'}}>{teamSuggestion.neededCount}</span>
            </p>
            {renderEmployeeList(teamSuggestion.assignedEmployees, 'Currently Assigned')}
            {teamSuggestion.neededCount > 0 && renderEmployeeList(teamSuggestion.suggestedEmployees, 'Suggested Employees')}
            {/* Add button/logic here to assign suggested employees */}
        </div>
    );

    return (
        <div style={{ padding: '20px', maxWidth: '900px', margin: 'auto' }}>
            <h1 style={{ color: '#333' }}>{project.name} - Assignment Board</h1>
            <div style={{ marginBottom: '20px', padding: '15px', background: '#f5f5f5', borderRadius: '5px'}}>
                 <p><strong>Status:</strong> {project.status || 'N/A'}</p>
                 <p><strong>Due Date:</strong> {project.dueDate ? new Date(project.dueDate).toLocaleDateString() : 'N/A'}</p>
                 {project.description && <p><strong>Description:</strong> {project.description}</p>}
            </div>

            <h2 style={{color: '#555', borderBottom: '2px solid #eee', paddingBottom: '10px'}}>Team Requirements & Suggestions</h2>
            {suggestions.teamSuggestions.length > 0 ? (
                suggestions.teamSuggestions.map(renderTeamSuggestion)
            ) : (
                <p>No specific team requirements or suggestions available for this project.</p>
            )}
        </div>
    );
};

export default ProjectDetailsPage; 