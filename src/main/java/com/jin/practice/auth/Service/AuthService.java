package com.jin.practice.auth.Service;

import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.dto.JwtDto;
import com.jin.practice.member.entity.Member;
import com.jin.practice.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    public JwtDto refresh(String authorization) {
        if(authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 없습니다.");
        }

        String refreshToken = authorization.substring(7);

        if(!jwtProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 token 입니다.");
        }

        String email = jwtProvider.getSubject(refreshToken);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );

        return jwtProvider.createToken(authentication);
    }

}
