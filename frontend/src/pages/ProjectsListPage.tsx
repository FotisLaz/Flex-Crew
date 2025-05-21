import React, { useEffect, useState } from 'react';
import { Project } from '../types/projectTypes';
import { projectService } from '../services/projectService';
import ProjectCard from '../components/projects/ProjectCard';
// import { Link } from 'react-router-dom'; // For a "Create New Project" button

const ProjectsListPage: React.FC = () => {
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchProjects = async () => {
            try {
                setLoading(true);
                const data = await projectService.getAllProjects();
                // Ensure data is an array before setting projects
                setProjects(Array.isArray(data) ? data : []);
                setError(null);
            } catch (err: any) {
                setError(err.message || 'Failed to fetch projects');
                setProjects([]); // Set to empty array on error as well
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchProjects();
    }, []);

    if (loading) return <p>Loading projects...</p>;
    // Keep the error check, but it might be more robust to check if projects is empty *after* error is null
    if (error) return <p style={{ color: 'red' }}>Error: {error}</p>;

    return (
        <div style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1 style={{ color: '#333' }}>Projects</h1>
                {/* <Link to="/projects/new" style={{ padding: '10px 15px', backgroundColor: '#007bff', color: 'white', textDecoration: 'none', borderRadius: '5px' }}>
                    Create New Project
                </Link> */}
            </div>
            {!Array.isArray(projects) || projects.length === 0 ? (
                <p>{error ? `Error: ${error}` : 'No projects found.'}</p> // Display error here if projects is not an array due to an error
            ) : (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px' }}>
                    {projects.map(project => (
                        <ProjectCard key={project.projectId} project={project} />
                    ))}
                </div>
            )}
        </div>
    );
};

export default ProjectsListPage; 