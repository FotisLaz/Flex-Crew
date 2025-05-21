package com.lazardev.FlexCrew.dao;

import com.lazardev.FlexCrew.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
    // Add custom query methods here if needed in the future
    // e.g., List<Issue> findByIssueStatusName(String statusName);
}
