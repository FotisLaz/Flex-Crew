package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.entity.Issue;
import com.lazardev.FlexCrew.service.IssueService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues") // Base path for issue-related endpoints
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Only logged-in users can see issues
    public ResponseEntity<?> getAllIssues() {
        try {
            List<Issue> issues = issueService.findAllIssues();
            // Note: Ensure that the Issue entity and its related entities (like IssueStatus)
            // are properly serialized to JSON. Jackson should handle this by default with Lombok getters.
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            // Log the exception
            // logger.error("Error fetching all issues", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while fetching issues.");
        }
    }

    // Add other endpoints like GET /api/issues/{id}, POST /api/issues etc. later if needed
} 