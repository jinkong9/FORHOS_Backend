package com.jin.practice.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 수정 요청")
public record UpdateMyInfoDto(
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "나이", example = "31")
        int age,
        @Schema(description = "성별", example = "MALE")
        String gender,
        @Schema(description = "전화번호", example = "010-9876-5432")
        String phone,
        @Schema(description = "지역", example = "Busan")
        String region,
        @Schema(description = "추가 정보", example = "업데이트된 정보")
        String extra
) {

}
