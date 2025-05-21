package com.lazardev.FlexCrew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeSuggestionDTO {
    private Integer employeeId;
    private String names;
    private String firstSurname;
    private String teamName; // The team they belong to
    // Add other relevant employee details you might want to show in suggestions
}