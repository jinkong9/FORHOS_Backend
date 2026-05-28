package com.jin.practice.member.service;

import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.dto.MedicalProfileDto;
import com.jin.practice.member.entity.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberServiceMedicalProfileTest {

    @Test
    void updateMedicalProfileStoresMedicinesDiseasesAllergiesAndNotes() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        MemberService memberService = new MemberService(memberRepository, null, null);
        Member member = member();

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));

        MedicalProfileDto response = memberService.updateMedicalProfile(
                "user@example.com",
                new MedicalProfileDto("Aspirin", "Diabetes", "Penicillin", "Needs wheelchair")
        );

        assertThat(response.medicines()).isEqualTo("Aspirin");
        assertThat(response.diseases()).isEqualTo("Diabetes");
        assertThat(response.allergies()).isEqualTo("Penicillin");
        assertThat(response.notes()).isEqualTo("Needs wheelchair");
    }

    @Test
    void getMedicalProfileReturnsEmptyProfileWhenNotSet() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        MemberService memberService = new MemberService(memberRepository, null, null);

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member()));

        MedicalProfileDto response = memberService.getMedicalProfile("user@example.com");

        assertThat(response.medicines()).isEmpty();
        assertThat(response.diseases()).isEmpty();
        assertThat(response.allergies()).isEmpty();
        assertThat(response.notes()).isEmpty();
    }

    private Member member() {
        return new Member(
                "user@example.com",
                "encoded-password",
                "User",
                30,
                "010-1234-5678",
                "MALE",
                "Seoul",
                "none",
                LocalDateTime.now()
        );
    }
}
