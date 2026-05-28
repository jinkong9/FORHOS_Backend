package com.jin.practice.recommendation.service;

import com.jin.practice.recommendation.dto.SymptomRecommendationDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SymptomRecommendationServiceTest {

    @Test
    void recommendsInternalMedicineForFeverAndCough() {
        SymptomRecommendationService service = new SymptomRecommendationService();

        SymptomRecommendationDto response = service.recommend("기침과 발열이 있어요");

        assertThat(response.department()).isEqualTo("내과");
        assertThat(response.reason()).contains("기침");
    }

    @Test
    void recommendsDermatologyForRash() {
        SymptomRecommendationService service = new SymptomRecommendationService();

        SymptomRecommendationDto response = service.recommend("피부 발진이 심해요");

        assertThat(response.department()).isEqualTo("피부과");
    }

    @Test
    void fallsBackToGeneralMedicineWhenNoKeywordMatches() {
        SymptomRecommendationService service = new SymptomRecommendationService();

        SymptomRecommendationDto response = service.recommend("몸이 이상해요");

        assertThat(response.department()).isEqualTo("일반진료");
    }
}
