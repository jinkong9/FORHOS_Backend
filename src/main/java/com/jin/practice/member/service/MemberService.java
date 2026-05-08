package com.jin.practice.member.service;

import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.dto.LoginDto;
import com.jin.practice.member.dto.MyDto;
import com.jin.practice.member.dto.RegisterDto;
import com.jin.practice.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long registerMember(RegisterDto registerDto){
        String encodedPassword = passwordEncoder.encode(registerDto.password());

        Member member = registerDto.toEntity(encodedPassword);

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }

    public Authentication loginMember(LoginDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 틀렸습니다."));

        if(!passwordEncoder.matches(loginDto.password(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 틀렸습니다.");
        }

        return new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public MyDto getMyName(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다"));

        return new MyDto(member.getName());
    }

}
