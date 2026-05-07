package com.jin.practice.member.dto;

import com.jin.practice.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record RegisterDto(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호은 필수 입력 값입니다.")
        String password,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        String phone

) {
        // builder pattern -> toEntity
        public Member toEntity(String encodedPassword) {
                return new Member(email, encodedPassword, name, phone, LocalDate.now());
        }
}
