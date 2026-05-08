package com.jin.practice.member.Controller;

import com.jin.practice.member.dto.JwtDto;
import com.jin.practice.member.dto.LoginDto;
import com.jin.practice.member.dto.MyDto;
import com.jin.practice.member.dto.RegisterDto;
import com.jin.practice.member.service.MemberService;
import com.jin.practice.util.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/my")
    public ResponseEntity<MyDto> getMyName(
            // @RequestHeader("Authorization")
            Authentication authentication
    ) {

        MyDto response = memberService.getMyName(authentication.getName());

        return ResponseEntity.ok(response);
    }
}
