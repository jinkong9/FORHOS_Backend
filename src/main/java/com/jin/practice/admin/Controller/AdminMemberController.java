package com.jin.practice.admin.Controller;

import com.jin.practice.admin.dto.HospitalAdminCreateDto;
import com.jin.practice.admin.dto.MemberHospitalAssignDto;
import com.jin.practice.admin.dto.MemberRoleUpdateDto;
import com.jin.practice.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {
    private final MemberService memberService;

    @PostMapping("/hospital-admin")
    public ResponseEntity<Long> createHospitalAdmin(
            @Valid @RequestBody HospitalAdminCreateDto dto
    ) {
        Long memberId = memberService.createHospitalAdmin(dto);
        return ResponseEntity.created(URI.create("/api/admin/members/" + memberId)).body(memberId);
    }

    @PatchMapping("/{memberId}/hospital")
    public ResponseEntity<Void> assignHospital(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberHospitalAssignDto dto
    ) {
        memberService.assignHospitalToMember(memberId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateDto dto
    ) {
        memberService.updateMemberRole(memberId, dto);
        return ResponseEntity.noContent().build();
    }
}
