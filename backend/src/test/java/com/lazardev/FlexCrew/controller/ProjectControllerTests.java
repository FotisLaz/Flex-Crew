package com.lazardev.FlexCrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazardev.FlexCrew.entity.Project;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser // Simulate an authenticated user
    void getAllProjects_shouldReturnListOfProjects() throws Exception {
        Project project1 = new Project();
        project1.setProjectId(1);
        project1.setName("Project Alpha");

        Project project2 = new Project();
        project2.setProjectId(2);
        project2.setName("Project Beta");

        List<Project> projects = Arrays.asList(project1, project2);

        given(projectService.getAllProjects()).willReturn(projects);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(projects.size()))
                .andExpect(jsonPath("$[0].name").value("Project Alpha"))
                .andExpect(jsonPath("$[1].name").value("Project Beta"));
    }

    // You can add more test methods here for other endpoints like:
    // - getProjectById
    // - createProject
    // - updateProject
    // - deleteProject
    // - getAssignmentSuggestions
    // Remember to use @WithMockUser or configure security appropriately for each test.
} 