package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.entity.Issue;
import java.util.List;
import java.util.Optional;

public interface IssueService {
    List<Issue> findAllIssues();

    Optional<Issue> findIssueById(Integer id);
    // Add other methods like saveIssue, deleteIssue, findByStatus etc. later if
    // needed
}