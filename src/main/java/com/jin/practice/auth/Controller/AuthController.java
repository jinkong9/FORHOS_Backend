package com.jin.practice.auth.Controller;

import com.jin.practice.auth.Service.AuthService;
import com.jin.practice.common.ErrorResponse;
import com.jin.practice.member.dto.JwtDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "JWT 인증 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 검증하고 새로운 JWT 토큰을 발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = JwtDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "리프레시 토큰 누락 또는 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<JwtDto> refreshToken(@RequestHeader("Authorization") String authorization) {
        JwtDto response = authService.refresh(authorization);
        return ResponseEntity.ok(response);
    }
}
