package com.lazardev.FlexCrew.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
}