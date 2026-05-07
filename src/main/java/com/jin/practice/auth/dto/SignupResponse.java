package com.jin.practice.auth.dto;

public record SignupResponse(
	Long id,
	String name,
	String email,
	String phone
) {
}
