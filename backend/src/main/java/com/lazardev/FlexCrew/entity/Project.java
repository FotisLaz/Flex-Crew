package com.lazardev.FlexCrew.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "\"Projects\"", schema = "Flex-Crew-v1")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 50)
    private String status = "OPEN"; // Default value

    @CreationTimestamp
    @Column(name = "creation_date", updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creationDate;

    @Column(name = "due_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime dueDate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("project-requiredTeams")
    private Set<ProjectRequiredTeam> requiredTeams;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("project-employeeAssignments")
    private Set<ProjectEmployeeAssignment> employeeAssignments;

    // Consider adding constructors, equals, hashCode, and toString methods as
    // needed
}