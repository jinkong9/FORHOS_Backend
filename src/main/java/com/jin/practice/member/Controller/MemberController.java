package com.jin.practice.member.Controller;

import com.jin.practice.member.dto.*;
import com.jin.practice.member.service.MemberService;
import com.jin.practice.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 가입, 로그인, 내 정보 API")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    @Operation(summary = "회원 가입", description = "새 회원을 등록합니다.")
    public ResponseEntity<String> registerMember(
            @Valid @RequestBody RegisterDto registerDto
    ) {
        Long memberId = memberService.registerMember(registerDto);

        URI location = URI.create("/api/members/" + memberId);
        return ResponseEntity.created(location).body("회원가입 완료");
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT를 발급합니다.")
    public ResponseEntity<JwtDto> loginMember(
            @Valid @RequestBody LoginDto loginDto
            ) {
        Authentication authentication = memberService.loginMember(loginDto);
        JwtDto jwtDto = jwtProvider.createToken(authentication);

        return ResponseEntity.ok(jwtDto);
    }

    @GetMapping("/myinfo")
    @Operation(
            summary = "내 정보 조회",
            description = "인증된 회원의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MyInfoDto> getMyInfo(
            // @RequestHeader("Authorization")
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
    public ResponseEntity<MyInfoDto> updateMyInfo (
            Authentication authentication,
            @RequestBody UpdateMyInfoDto updateMyInfoDto
    ) {
        MyInfoDto response = memberService.updateInfo(authentication.getName(), updateMyInfoDto);

        return ResponseEntity.ok(response);
    }
}
