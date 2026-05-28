package com.jin.practice.member.service;

import com.jin.practice.admin.dto.HospitalAdminCreateDto;
import com.jin.practice.admin.dto.MemberHospitalAssignDto;
import com.jin.practice.admin.dto.MemberRoleUpdateDto;
import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.entity.Member;
import com.jin.practice.member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberServiceAdminTest {

    @Test
    void createHospitalAdminAssignsRoleAndHospital() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        MemberService memberService = new MemberService(memberRepository, passwordEncoder, hospitalRepository);
        Hospital hospital = hospitalWithId(1L);

        when(memberRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(memberRepository.existsByPhone("010-1111-2222")).thenReturn(false);
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(passwordEncoder.encode("password1234")).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 10L);
            return member;
        });

        Long memberId = memberService.createHospitalAdmin(new HospitalAdminCreateDto(
                "admin@example.com",
                "password1234",
                "Admin",
                35,
                "010-1111-2222",
                "NONE",
                "Seoul",
                "staff",
                1L
        ));

        assertThat(memberId).isEqualTo(10L);
    }

    @Test
    void assignHospitalToMemberUpdatesHospital() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        MemberService memberService = new MemberService(memberRepository, null, hospitalRepository);
        Member member = memberWithRole(MemberRole.HOSPITAL_ADMIN);
        Hospital hospital = hospitalWithId(2L);

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));
        when(hospitalRepository.findById(2L)).thenReturn(Optional.of(hospital));

        memberService.assignHospitalToMember(10L, new MemberHospitalAssignDto(2L));

        assertThat(member.getHospital().getId()).isEqualTo(2L);
    }

    @Test
    void updateMemberRoleChangesStoredRole() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        MemberService memberService = new MemberService(memberRepository, null, null);
        Member member = memberWithRole(MemberRole.USER);

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        memberService.updateMemberRole(10L, new MemberRoleUpdateDto(MemberRole.ADMIN));

        assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    private Member memberWithRole(MemberRole role) {
        Member member = new Member(
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
        ReflectionTestUtils.setField(member, "id", 10L);
        ReflectionTestUtils.setField(member, "role", role);
        return member;
    }

    private Hospital hospitalWithId(Long id) {
        Hospital hospital = new Hospital();
        ReflectionTestUtils.setField(hospital, "id", id);
        ReflectionTestUtils.setField(hospital, "name", "FORHOS Hospital");
        return hospital;
    }
}
