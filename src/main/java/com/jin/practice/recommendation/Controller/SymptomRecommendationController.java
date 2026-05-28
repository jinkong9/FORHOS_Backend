package com.jin.practice.recommendation.Controller;

import com.jin.practice.recommendation.dto.SymptomRecommendationDto;
import com.jin.practice.recommendation.dto.SymptomRecommendationRequestDto;
import com.jin.practice.recommendation.service.SymptomRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class SymptomRecommendationController {
    private final SymptomRecommendationService symptomRecommendationService;

    @PostMapping("/departments")
    public ResponseEntity<SymptomRecommendationDto> recommendDepartment(
            @Valid @RequestBody SymptomRecommendationRequestDto request
    ) {
        return ResponseEntity.ok(symptomRecommendationService.recommend(request.symptom()));
    }
}
