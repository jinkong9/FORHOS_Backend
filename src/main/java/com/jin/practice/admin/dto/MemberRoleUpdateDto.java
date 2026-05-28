package com.jin.practice.admin.dto;

import com.jin.practice.member.entity.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateDto(
        @NotNull
        MemberRole role
) {
}
