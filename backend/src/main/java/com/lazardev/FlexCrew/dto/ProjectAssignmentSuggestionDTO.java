package com.lazardev.FlexCrew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAssignmentSuggestionDTO {
    private Integer projectId;
    private String projectName;
    private List<RequiredTeamSuggestionDTO> teamSuggestions;
    // You can add overall project status or other summary details here
}