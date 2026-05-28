package com.jin.practice.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SymptomRecommendationRequestDto(
        @NotBlank(message = "증상을 입력해 주세요.")
        @Size(max = 500, message = "증상은 500자 이하로 입력해 주세요.")
        String symptom
) {
}
