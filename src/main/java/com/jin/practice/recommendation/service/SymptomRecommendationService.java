package com.jin.practice.recommendation.service;

import com.jin.practice.recommendation.dto.SymptomRecommendationDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SymptomRecommendationService {
    private final List<Rule> rules = List.of(
            new Rule("내과", List.of("기침", "발열", "감기", "복통", "두통")),
            new Rule("치과", List.of("치통", "잇몸", "충치")),
            new Rule("피부과", List.of("피부", "발진", "가려움", "두드러기")),
            new Rule("정형외과", List.of("골절", "허리", "무릎", "관절", "삐끗")),
            new Rule("이비인후과", List.of("목", "코", "귀", "비염", "인후통"))
    );

    public SymptomRecommendationDto recommend(String symptom) {
        String normalized = symptom == null ? "" : symptom.toLowerCase();

        for (Rule rule : rules) {
            for (String keyword : rule.keywords()) {
                if (normalized.contains(keyword.toLowerCase())) {
                    return new SymptomRecommendationDto(
                            rule.department(),
                            "'" + keyword + "' 증상 키워드를 기준으로 추천했습니다."
                    );
                }
            }
        }

        return new SymptomRecommendationDto(
                "일반진료",
                "정확히 일치하는 증상 키워드가 없어 일반진료를 추천했습니다."
        );
    }

    private record Rule(String department, List<String> keywords) {
    }
}
