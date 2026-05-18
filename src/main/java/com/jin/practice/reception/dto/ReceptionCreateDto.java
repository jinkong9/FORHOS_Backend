package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.VisitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "접수 생성 요청")
public record ReceptionCreateDto(
        @NotNull(message = "병원을 선택해 주세요.")
        @Schema(description = "병원 ID", example = "1")
        Long hospitalId,

        @NotBlank(message = "환자 이름을 입력해 주세요.")
        @Schema(description = "환자 이름", example = "홍길동")
        String patientName,

        @NotNull(message = "방문 유형을 선택해 주세요.")
        @Schema(description = "방문 유형", example = "FIRST", allowableValues = {"FIRST", "RETURN"})
        VisitType visitType,

        @NotBlank(message = "증상을 입력해 주세요.")
        @Size(max = 500, message = "증상은 500자 이하로 입력해 주세요.")
        @Schema(description = "증상", example = "기침과 발열")
        String symptom
) {
}
