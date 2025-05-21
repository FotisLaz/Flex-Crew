package com.lazardev.FlexCrew.dao;

import com.lazardev.FlexCrew.entity.ProjectEmployeeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectEmployeeAssignmentRepository extends JpaRepository<ProjectEmployeeAssignment, Integer> {
    List<ProjectEmployeeAssignment> findByProjectProjectId(Integer projectId);

    List<ProjectEmployeeAssignment> findByEmployeeId(Integer employeeId);
    // You can add other custom query methods here if needed
}