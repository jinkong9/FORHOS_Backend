package com.jin.practice.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HospitalAdminCreateDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        @NotBlank
        String name,
        @NotNull
        @Min(0)
        Integer age,
        @NotBlank
        String phone,
        @NotBlank
        String gender,
        String region,
        String extra,
        @NotNull
        Long hospitalId
) {
}
