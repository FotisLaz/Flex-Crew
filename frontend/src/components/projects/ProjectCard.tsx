import React from 'react';
import { Project } from '../../types/projectTypes';
import { Link } from 'react-router-dom'; // Assuming you use React Router for navigation

interface ProjectCardProps {
    project: Project;
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project }) => {
    return (
        <div style={{ border: '1px solid #ccc', borderRadius: '8px', padding: '16px', margin: '16px', width: '300px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginTop: 0 }}>{project.name}</h3>
            <p><strong>Status:</strong> {project.status || 'N/A'}</p>
            <p><strong>Due Date:</strong> {project.dueDate ? new Date(project.dueDate).toLocaleDateString() : 'N/A'}</p>
            {project.description && <p style={{ fontSize: '0.9em', color: '#555' }}>{project.description.substring(0,100)}{project.description.length > 100 ? '...' : ''}</p>}
            <Link to={`/projects/${project.projectId}`} style={{ textDecoration: 'none', color: '#007bff', fontWeight: 'bold' }}>
                View Details & Suggestions
            </Link>
        </div>
    );
};

export default ProjectCard; 