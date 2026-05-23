package com.jin.practice.member.service;

import com.jin.practice.auth.Service.AuthService;
import com.jin.practice.auth.jwt.JwtProvider;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.dto.JwtDto;
import com.jin.practice.member.dto.LoginDto;
import com.jin.practice.member.entity.Member;
import com.jin.practice.member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberServiceRoleTest {

    private static final String SECRET = "fb81881510ce7b9460a7b0e13a55c1d6682aba4a0db1eab2f1eab2f1e29afe1cfda94b";

    @Test
    void loginUsesStoredMemberRoleAsAuthority() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Member member = memberWithRole(MemberRole.HOSPITAL_ADMIN);
        MemberService memberService = new MemberService(memberRepository, passwordEncoder);

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1234", "encoded-password")).thenReturn(true);

        Authentication authentication = memberService.loginMember(new LoginDto("admin@example.com", "password1234"));

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_HOSPITAL_ADMIN");
    }

    @Test
    void refreshUsesStoredMemberRoleAsAuthority() {
        JwtProvider jwtProvider = new JwtProvider(SECRET, 3600000L);
        MemberRepository memberRepository = mock(MemberRepository.class);
        AuthService authService = new AuthService(jwtProvider, memberRepository);
        Member member = memberWithRole(MemberRole.HOSPITAL_ADMIN);
        Authentication userAuthentication = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        JwtDto tokens = jwtProvider.createToken(userAuthentication);

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(member));

        JwtDto refreshedTokens = authService.refresh("Bearer " + tokens.refreshToken());
        Authentication refreshedAuthentication = jwtProvider.getAuthentication(refreshedTokens.accessToken());

        assertThat(refreshedAuthentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_HOSPITAL_ADMIN");
    }

    private Member memberWithRole(MemberRole role) {
        Member member = new Member(
                "admin@example.com",
                "encoded-password",
                "Admin",
                30,
                "010-1234-5678",
                "none",
                "seoul",
                "",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(member, "role", role);
        return member;
    }
}
