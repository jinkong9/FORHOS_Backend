package com.jin.practice.member.dto;

import com.jin.practice.member.entity.MemberMedicalProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의료 정보 프로필")
public record MedicalProfileDto(
        @Schema(description = "복용 중인 약", example = "Aspirin")
        String medicines,
        @Schema(description = "기존 질병", example = "Diabetes")
        String diseases,
        @Schema(description = "알레르기", example = "Penicillin")
        String allergies,
        @Schema(description = "특이사항", example = "Needs wheelchair")
        String notes
) {
    public static MedicalProfileDto from(MemberMedicalProfile profile) {
        if (profile == null) {
            return empty();
        }

        return new MedicalProfileDto(
                profile.getMedicines(),
                profile.getDiseases(),
                profile.getAllergies(),
                profile.getNotes()
        );
    }

    public static MedicalProfileDto empty() {
        return new MedicalProfileDto("", "", "", "");
    }
}
