package com.lazardev.FlexCrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Project;
import com.lazardev.FlexCrew.entity.Role;
import com.lazardev.FlexCrew.security.SecurityRulesTests;
import com.lazardev.FlexCrew.security.config.ApplicationConfig;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.config.SecurityConfiguration;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import({SecurityConfiguration.class, ApplicationConfig.class, JwtService.class})
@TestPropertySource(
        properties = "application.security.jwt.secret-key=" + SecurityRulesTests.TEST_KEY)
class ProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private TokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /** Signs a real JWT for a regular user and stores it as a valid token, like a fresh login. */
    private String userBearer() {
        Employee user = Employee.builder()
                .id(1)
                .email("user@test.local")
                .role(Role.USER)
                .build();
        given(employeeRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        String jwt = jwtService.generateToken(user);
        given(tokenRepository.findByToken(jwt))
                .willReturn(Optional.of(Token.builder().token(jwt).employee(user).build()));
        return "Bearer " + jwt;
    }

    @Test
    void getAllProjects_shouldReturnListOfProjects() throws Exception {
        Project project1 = new Project();
        project1.setProjectId(1);
        project1.setName("Project Alpha");

        Project project2 = new Project();
        project2.setProjectId(2);
        project2.setName("Project Beta");

        List<Project> projects = Arrays.asList(project1, project2);

        given(projectService.getAllProjects()).willReturn(projects);

        mockMvc.perform(get("/api/v1/projects").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(projects.size()))
                .andExpect(jsonPath("$[0].name").value("Project Alpha"))
                .andExpect(jsonPath("$[1].name").value("Project Beta"));
    }
}
