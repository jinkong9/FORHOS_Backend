package com.jin.practice.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 응답")
public record JwtDto(
        @Schema(description = "토큰 타입", example = "Bearer")
        String grantType,
        @Schema(description = "액세스 토큰")
        String accessToken,
        @Schema(description = "리프레시 토큰")
        String refreshToken
) {
}
