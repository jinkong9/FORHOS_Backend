package com.jin.practice.member.Controller;

import com.jin.practice.common.ErrorResponse;
import com.jin.practice.member.dto.JwtDto;
import com.jin.practice.member.dto.LoginDto;
import com.jin.practice.member.dto.MyInfoDto;
import com.jin.practice.member.dto.RegisterDto;
import com.jin.practice.member.dto.UpdateMyInfoDto;
import com.jin.practice.member.service.MemberService;
import com.jin.practice.auth.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원가입, 로그인, 내 정보 API")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일 또는 전화번호",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<String> registerMember(
            @Valid @RequestBody RegisterDto registerDto
    ) {
        Long memberId = memberService.registerMember(registerDto);

        URI location = URI.create("/api/members/" + memberId);
        return ResponseEntity.created(location).body("회원가입 완료");
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT를 발급합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = JwtDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<JwtDto> loginMember(
            @Valid @RequestBody LoginDto loginDto
    ) {
        Authentication authentication = memberService.loginMember(loginDto);
        JwtDto jwtDto = jwtProvider.createToken(authentication);

        return ResponseEntity.ok(jwtDto);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "브라우저에 저장된 인증 쿠키를 만료시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    })
    public ResponseEntity<Void> logoutMember(HttpServletResponse response) {
        ResponseCookie accessToken = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshToken = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie grantType = ResponseCookie.from("grant_type", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, grantType.toString());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myinfo")
    @Operation(
            summary = "내 정보 조회",
            description = "인증된 회원의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = MyInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MyInfoDto> getMyInfo(
            Authentication authentication
    ) {

        MyInfoDto response = memberService.getMyInfo(authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/myinfo")
    @Operation(
            summary = "내 정보 수정",
            description = "인증된 회원의 정보를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = MyInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MyInfoDto> updateMyInfo(
            Authentication authentication,
            @RequestBody UpdateMyInfoDto updateMyInfoDto
    ) {
        MyInfoDto response = memberService.updateInfo(authentication.getName(), updateMyInfoDto);

        return ResponseEntity.ok(response);
    }
}
