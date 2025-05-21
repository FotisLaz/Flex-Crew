package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.dao.IssueRepository;
import com.lazardev.FlexCrew.entity.Issue;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;

    @Override
    public List<Issue> findAllIssues() {
        // Important: Fetch associated IssueStatus eagerly if needed often,
        // or handle potential N+1 queries if fetched lazily.
        // For now, JpaRepository's findAll() might fetch lazily.
        // Consider adding @EntityGraph in Repository if performance becomes an issue.
        return issueRepository.findAll();
    }

    @Override
    public Optional<Issue> findIssueById(Integer id) {
        return issueRepository.findById(id);
    }

    // Implement other methods from the interface here when needed
}