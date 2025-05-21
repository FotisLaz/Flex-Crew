package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.dto.ProjectAssignmentSuggestionDTO;
import com.lazardev.FlexCrew.entity.Project;
import com.lazardev.FlexCrew.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects") // Using /api/v1 for versioning practice
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable Integer projectId) {
        return projectService.getProjectById(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        // Add validation for the project object here (e.g., using @Valid)
        Project createdProject = projectService.createProject(project);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable Integer projectId, @RequestBody Project projectDetails) {
        // Add validation for the projectDetails object here
        try {
            Project updatedProject = projectService.updateProject(projectId, projectDetails);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) { // Replace with specific exceptions
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer projectId) {
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) { // Replace with specific exceptions
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{projectId}/assignment-suggestions")
    public ResponseEntity<ProjectAssignmentSuggestionDTO> getAssignmentSuggestions(@PathVariable Integer projectId) {
        try {
            ProjectAssignmentSuggestionDTO suggestions = projectService.getProjectAssignmentSuggestions(projectId);
            return ResponseEntity.ok(suggestions);
        } catch (RuntimeException e) { // Replace with specific exceptions
            // Log the exception e.g. logger.error("Error getting suggestions for project
            // {}", projectId, e);
            return ResponseEntity.notFound().build(); // Or internal server error depending on exception type
        }
    }
}