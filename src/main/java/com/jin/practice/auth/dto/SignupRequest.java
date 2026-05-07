package com.jin.practice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@NotBlank(message = "이름을 입력해 주세요.")
	@Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해 주세요.")
	String name,

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "올바른 이메일을 입력해 주세요.")
	String email,

	@NotBlank(message = "휴대폰 번호를 입력해 주세요.")
	@Pattern(regexp = "^01\\d-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
	String phone,

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
	String password,

	@NotBlank(message = "비밀번호 확인을 입력해 주세요.")
	String passwordConfirm
) {
}
