package com.jin.practice.member.Controller;

import com.jin.practice.member.dto.RegisterDto;
import com.jin.practice.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<String> registerMember(
            @Valid @RequestBody RegisterDto registerDto
    ) {
        Long memberId = memberService.registerMember(registerDto);

        URI location = URI.create("/api/members/" + memberId);
        return ResponseEntity.created(location).body("회원가입 완료");
    }

//    @PostMapping("/login")
//    public
}
