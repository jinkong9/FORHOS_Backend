package com.jin.practice.member.dto;

import com.jin.practice.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RegisterDto(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 맞지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호은 필수 입력 값입니다.")
        String password,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        String name,

        @NotNull(message = "나이는 필수 입력 값입니다.")
        @Min(value = 0, message = "나이는 0보다 커야합니다.")
        Integer age,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        String phone,

        @NotBlank(message = "성별은 필수 입력 값입니다.")
        String gender,

        String region,

        String extra
) {
        public Member toEntity(String encodedPassword) {
                return new Member(email, encodedPassword, name, age, phone, gender, region, extra, LocalDateTime.now());
        }
}
