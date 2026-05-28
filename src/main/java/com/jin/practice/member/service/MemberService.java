package com.jin.practice.member.service;

import com.jin.practice.admin.dto.HospitalAdminCreateDto;
import com.jin.practice.admin.dto.MemberHospitalAssignDto;
import com.jin.practice.admin.dto.MemberRoleUpdateDto;
import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.dto.LoginDto;
import com.jin.practice.member.dto.MedicalProfileDto;
import com.jin.practice.member.dto.MyInfoDto;
import com.jin.practice.member.dto.RegisterDto;
import com.jin.practice.member.dto.UpdateMyInfoDto;
import com.jin.practice.member.entity.Member;
import com.jin.practice.member.entity.MemberMedicalProfile;
import com.jin.practice.member.entity.MemberRole;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final HospitalRepository hospitalRepository;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this(memberRepository, passwordEncoder, null);
    }

    @Autowired
    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            HospitalRepository hospitalRepository
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.hospitalRepository = hospitalRepository;
    }

    public Long registerMember(RegisterDto registerDto) {
        validateUniqueMember(registerDto.email(), registerDto.phone());

        String encodedPassword = passwordEncoder.encode(registerDto.password());
        Member member = registerDto.toEntity(encodedPassword);
        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }

    @Transactional
    public Long createHospitalAdmin(HospitalAdminCreateDto dto) {
        validateUniqueMember(dto.email(), dto.phone());

        Hospital hospital = findHospitalById(dto.hospitalId());
        Member member = new Member(
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.name(),
                dto.age(),
                dto.phone(),
                dto.gender(),
                dto.region(),
                dto.extra(),
                LocalDateTime.now()
        );
        member.updateRole(MemberRole.HOSPITAL_ADMIN);
        member.assignHospital(hospital);

        return memberRepository.save(member).getId();
    }

    @Transactional
    public void assignHospitalToMember(Long memberId, MemberHospitalAssignDto dto) {
        Member member = findMemberById(memberId);
        Hospital hospital = findHospitalById(dto.hospitalId());

        member.assignHospital(hospital);
    }

    @Transactional
    public void updateMemberRole(Long memberId, MemberRoleUpdateDto dto) {
        Member member = findMemberById(memberId);
        member.updateRole(dto.role());
    }

    public Authentication loginMember(LoginDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 맞지 않습니다."));

        if (!passwordEncoder.matches(loginDto.password(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 맞지 않습니다.");
        }

        return new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
    }

    public MyInfoDto getMyInfo(String email) {
        return MyInfoDto.from(findMemberByEmail(email));
    }

    @Transactional
    public MyInfoDto updateInfo(String email, UpdateMyInfoDto updateMyInfoDto) {
        Member member = findMemberByEmail(email);

        member.updateInfo(
                updateMyInfoDto.name(),
                updateMyInfoDto.age(),
                updateMyInfoDto.gender(),
                updateMyInfoDto.phone(),
                updateMyInfoDto.region(),
                updateMyInfoDto.extra()
        );

        return MyInfoDto.from(member);
    }

    public MedicalProfileDto getMedicalProfile(String email) {
        return MedicalProfileDto.from(findMemberByEmail(email).getMedicalProfile());
    }

    @Transactional
    public MedicalProfileDto updateMedicalProfile(String email, MedicalProfileDto dto) {
        Member member = findMemberByEmail(email);
        MemberMedicalProfile profile = new MemberMedicalProfile(
                dto.medicines(),
                dto.diseases(),
                dto.allergies(),
                dto.notes()
        );

        member.updateMedicalProfile(profile);

        return MedicalProfileDto.from(profile);
    }

    private void validateUniqueMember(String email, String phone) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if (memberRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."));
    }

    private Hospital findHospitalById(Long hospitalId) {
        if (hospitalRepository == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "병원 저장소가 설정되지 않았습니다.");
        }

        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "병원을 찾을 수 없습니다."));
    }
}
