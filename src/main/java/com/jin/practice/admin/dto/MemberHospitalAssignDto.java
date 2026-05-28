package com.jin.practice.admin.dto;

import jakarta.validation.constraints.NotNull;

public record MemberHospitalAssignDto(
        @NotNull
        Long hospitalId
) {
}
