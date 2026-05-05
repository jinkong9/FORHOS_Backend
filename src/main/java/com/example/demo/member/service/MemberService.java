package com.example.demo.member.service;

import com.example.demo.member.Repository.MemberRepository;
import com.example.demo.member.dto.RegisterDto;
import com.example.demo.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Long registerMember(RegisterDto registerDto){
        LocalDate time = LocalDate.now();
        Member member = new Member(registerDto.email(), registerDto.password(), registerDto.name(), registerDto.phone(),time);

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }
}
