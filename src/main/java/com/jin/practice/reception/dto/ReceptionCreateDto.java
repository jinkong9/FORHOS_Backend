package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.VisitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReceptionCreateDto(
        @NotNull(message = "병원을 선택해 주세요.")
        Long hospitalId,

        @NotBlank(message = "환자 이름을 입력해 주세요.")
        String patientName,

        @NotNull(message = "방문 유형을 선택해 주세요.")
        VisitType visitType,

        @NotBlank(message = "증상을 입력해 주세요.")
        @Size(max = 500, message = "증상은 500자 이하로 입력해 주세요.")
        String symptom
) {
}
