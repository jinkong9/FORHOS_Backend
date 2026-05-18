package com.jin.practice.member.dto;

import com.jin.practice.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "회원 가입 요청")
public record RegisterDto(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 맞지 않습니다.")
        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @NotBlank(message = "비밀번호은 필수 입력 값입니다.")
        @Schema(description = "비밀번호", example = "password1234")
        String password,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotNull(message = "나이는 필수 입력 값입니다.")
        @Min(value = 0, message = "나이는 0보다 커야합니다.")
        @Schema(description = "나이", example = "30")
        Integer age,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @NotBlank(message = "성별은 필수 입력 값입니다.")
        @Schema(description = "성별", example = "MALE")
        String gender,

        @Schema(description = "지역", example = "Seoul")
        String region,

        @Schema(description = "추가 정보", example = "특이사항 없음")
        String extra
) {
        public Member toEntity(String encodedPassword) {
                return new Member(email, encodedPassword, name, age, phone, gender, region, extra, LocalDateTime.now());
        }
}
