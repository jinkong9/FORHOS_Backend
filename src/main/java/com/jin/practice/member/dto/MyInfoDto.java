package com.jin.practice.member.dto;

import com.jin.practice.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 응답")
public record MyInfoDto(
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "나이", example = "30")
        int age,
        @Schema(description = "성별", example = "MALE")
        String gender,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,
        @Schema(description = "지역", example = "Seoul")
        String region,
        @Schema(description = "추가 정보", example = "특이사항 없음")
        String extra
) {
    public static MyInfoDto from(Member member) {
        return new MyInfoDto(
                member.getName(),
                member.getAge(),
                member.getGender(),
                member.getPhone(),
                member.getRegion(),
                member.getExtra()
        );
    }

}
