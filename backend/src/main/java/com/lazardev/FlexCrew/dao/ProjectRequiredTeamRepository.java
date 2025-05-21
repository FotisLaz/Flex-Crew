package com.lazardev.FlexCrew.dao;

import com.lazardev.FlexCrew.entity.ProjectRequiredTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRequiredTeamRepository extends JpaRepository<ProjectRequiredTeam, Integer> {
    List<ProjectRequiredTeam> findByProjectProjectId(Integer projectId);
    // You can add other custom query methods here if needed
}