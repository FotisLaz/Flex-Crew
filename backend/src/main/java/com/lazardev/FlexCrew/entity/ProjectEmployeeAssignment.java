package com.lazardev.FlexCrew.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "Project_Employee_Assignments", schema = "Flex-Crew-v1", uniqueConstraints = @UniqueConstraint(columnNames = {
        "fk_project_id", "fk_employee_id" }))
@Getter
@Setter
@NoArgsConstructor
public class ProjectEmployeeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_project_id", nullable = false)
    @JsonBackReference("project-employeeAssignments")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_employee_id", nullable = false)
    private Employee employee; // Assuming you have an Employee entity

    @CreationTimestamp
    @Column(name = "assigned_date", updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime assignedDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Consider adding constructors, equals, hashCode, and toString methods as
    // needed
    // Ensure 'Employee' entity exists and is correctly mapped.
}