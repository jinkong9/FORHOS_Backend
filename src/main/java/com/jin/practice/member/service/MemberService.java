package com.jin.practice.member.service;

import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.dto.RegisterDto;
import com.jin.practice.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Member LoginMember(RegisterDto registerDto) {
        Optional<Member> optionalMember = memberRepository.findbyEmail(registerDto.email());

        if(optionalMember.isEmpty()) {
            return null;
        }

        Member member = optionalMember.get();

        if(!member.getPassword().equals(registerDto.password())) {
            return null;
        }

        return member;
    }



}
