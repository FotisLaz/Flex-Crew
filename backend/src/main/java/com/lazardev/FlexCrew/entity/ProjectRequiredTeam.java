package com.lazardev.FlexCrew.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"Project_Required_Teams\"", schema = "Flex-Crew-v1", uniqueConstraints = @UniqueConstraint(columnNames = {
        "fk_project_id", "fk_team_id" }))
@Getter
@Setter
@NoArgsConstructor
public class ProjectRequiredTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_required_team_id")
    private Integer projectRequiredTeamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_project_id", nullable = false)
    @JsonBackReference("project-requiredTeams")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_team_id", nullable = false)
    @JsonIgnore
    private Team team; // Assuming you have a Team entity

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    // Consider adding constructors, equals, hashCode, and toString methods as
    // needed
    // Ensure 'Team' entity exists and is correctly mapped.
}