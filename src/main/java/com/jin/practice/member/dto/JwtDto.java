package com.jin.practice.member.dto;

public record JwtDto(
        String grantType,
        String accessToken,
        String refreshToken
) {
}
