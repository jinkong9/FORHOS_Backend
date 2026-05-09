package com.jin.practice.member.Controller;

import com.jin.practice.member.dto.*;
import com.jin.practice.member.service.MemberService;
import com.jin.practice.util.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ResponseEntity<String> registerMember(
            @Valid @RequestBody RegisterDto registerDto
    ) {
        Long memberId = memberService.registerMember(registerDto);

        URI location = URI.create("/api/members/" + memberId);
        return ResponseEntity.created(location).body("회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDto> loginMember(
            @Valid @RequestBody LoginDto loginDto
            ) {
        Authentication authentication = memberService.loginMember(loginDto);
        JwtDto jwtDto = jwtProvider.createToken(authentication);

        return ResponseEntity.ok(jwtDto);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<MyInfoDto> getMyInfo(
            // @RequestHeader("Authorization")
            Authentication authentication
    ) {

        MyInfoDto response = memberService.getMyInfo(authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/myinfo")
    public ResponseEntity<MyInfoDto> updateMyInfo (
            Authentication authentication,
            @RequestBody UpdateMyInfoDto updateMyInfoDto
    ) {
        MyInfoDto response = memberService.updateInfo(authentication.getName(), updateMyInfoDto);

        return ResponseEntity.ok(response);
    }
}
