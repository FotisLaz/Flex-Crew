package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.dao.ProjectEmployeeAssignmentRepository;
import com.lazardev.FlexCrew.dao.ProjectRepository;
import com.lazardev.FlexCrew.dao.ProjectRequiredTeamRepository;
import com.lazardev.FlexCrew.dto.EmployeeSuggestionDTO;
import com.lazardev.FlexCrew.dto.ProjectAssignmentSuggestionDTO;
import com.lazardev.FlexCrew.dto.RequiredTeamSuggestionDTO;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Project;
import com.lazardev.FlexCrew.entity.ProjectEmployeeAssignment;
import com.lazardev.FlexCrew.entity.ProjectRequiredTeam;
import com.lazardev.FlexCrew.entity.Team; // Assuming Team entity is imported
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRequiredTeamRepository projectRequiredTeamRepository;
    private final ProjectEmployeeAssignmentRepository projectEmployeeAssignmentRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
            ProjectRequiredTeamRepository projectRequiredTeamRepository,
            ProjectEmployeeAssignmentRepository projectEmployeeAssignmentRepository,
            EmployeeRepository employeeRepository) {
        this.projectRepository = projectRepository;
        this.projectRequiredTeamRepository = projectRequiredTeamRepository;
        this.projectEmployeeAssignmentRepository = projectEmployeeAssignmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(Integer projectId) {
        return projectRepository.findById(projectId);
    }

    @Transactional
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Integer projectId, Project projectDetails) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setStatus(projectDetails.getStatus());
        project.setDueDate(projectDetails.getDueDate());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Integer projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }

    @Transactional(readOnly = true)
    public ProjectAssignmentSuggestionDTO getProjectAssignmentSuggestions(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        List<ProjectRequiredTeam> requiredTeams = projectRequiredTeamRepository.findByProjectProjectId(projectId);
        List<ProjectEmployeeAssignment> currentAssignments = projectEmployeeAssignmentRepository
                .findByProjectProjectId(projectId);

        Map<Integer, List<Employee>> assignmentsByTeamId = currentAssignments.stream()
                .filter(assignment -> assignment.getEmployee() != null && assignment.getEmployee().getTeam() != null)
                .collect(Collectors.groupingBy(assignment -> assignment.getEmployee().getTeam().getId(), // Assuming
                                                                                                         // Team has
                                                                                                         // getId()
                        Collectors.mapping(ProjectEmployeeAssignment::getEmployee, Collectors.toList())));

        List<RequiredTeamSuggestionDTO> teamSuggestionDTOs = new ArrayList<>();

        for (ProjectRequiredTeam prt : requiredTeams) {
            if (prt.getTeam() == null)
                continue; // Skip if team is somehow null

            Integer teamId = prt.getTeam().getId(); // Assuming Team has getId()
            String teamName = prt.getTeam().getName(); // Assuming Team has getName()
            int requiredCount = prt.getRequiredCount();

            List<Employee> assignedToThisTeamForProject = assignmentsByTeamId.getOrDefault(teamId, new ArrayList<>());
            int assignedCount = assignedToThisTeamForProject.size();
            int neededCount = Math.max(0, requiredCount - assignedCount);

            List<EmployeeSuggestionDTO> assignedEmployeeDTOs = assignedToThisTeamForProject.stream()
                    .map(emp -> new EmployeeSuggestionDTO(emp.getId(), emp.getNames(), emp.getFirstSurname(),
                            (emp.getTeam() != null ? emp.getTeam().getName() : "N/A")))
                    .collect(Collectors.toList());

            List<EmployeeSuggestionDTO> suggestedEmployeeDTOs = new ArrayList<>();
            if (neededCount > 0) {
                List<Employee> availableEmployeesFromTeam = employeeRepository.findByTeamId(teamId).stream() // Changed
                                                                                                             // to
                                                                                                             // findByTeamId
                        .filter(emp -> emp != null && emp.getId() != null && currentAssignments.stream()
                                .noneMatch(
                                        assign -> assign.getEmployee() != null && assign.getEmployee().getId() != null
                                                && assign.getEmployee().getId().equals(emp.getId())))
                        .limit(neededCount * 2)
                        .collect(Collectors.toList());

                suggestedEmployeeDTOs = availableEmployeesFromTeam.stream()
                        .map(emp -> new EmployeeSuggestionDTO(emp.getId(), emp.getNames(),
                                emp.getFirstSurname(), (emp.getTeam() != null ? emp.getTeam().getName() : "N/A")))
                        .collect(Collectors.toList());
            }

            teamSuggestionDTOs.add(new RequiredTeamSuggestionDTO(
                    teamId, teamName, requiredCount, assignedCount, neededCount,
                    assignedEmployeeDTOs, suggestedEmployeeDTOs));
        }

        return new ProjectAssignmentSuggestionDTO(project.getProjectId(), project.getName(), teamSuggestionDTOs);
    }
}