package com.lazardev.FlexCrew.dao;

import com.lazardev.FlexCrew.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    // You can add custom query methods here if needed in the future
    // For example:
    // List<Project> findByStatus(String status);
}