package com.jin.practice.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginDto(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @NotBlank(message = "비밀번호은 필수 입력 값입니다.")
        @Schema(description = "비밀번호", example = "password1234")
        String password

) {
}
