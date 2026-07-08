package com.jin.practice.reception.Controller;

import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.service.ReceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/receptions")
@RequiredArgsConstructor
@Tag(name = "Admin Reception", description = "병원 운영자용 대기열 관리 API")
public class AdminReceptionController {
    private final ReceptionService receptionService;

    @GetMapping
    @Operation(
            summary = "?대떦 蹂묒썝 ?묒닔 紐⑸줉 寃索",
            description = "날짜와 상태 조건으로 관리자 접수 목록을 페이징 조회합니다. 병원 관리자는 자기 병원만, 전체 관리자는 모든 병원을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ReceptionDto>> getReceptions(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) QueueStatus status,
            @PageableDefault(size = 20, sort = "queueNumber") Pageable pageable
    ) {
        return ResponseEntity.ok(
                receptionService.getReceptionsForHospitalAdmin(
                        authentication.getName(),
                        date,
                        status,
                        pageable
                )
        );
    }

    @GetMapping("/today")
    @Operation(
            summary = "담당 병원 오늘 대기열 조회",
            description = "로그인한 병원 운영자의 담당 병원 오늘 접수 대기열을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<ReceptionDto>> getMyHospitalTodayQueue(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                receptionService.getTodayReceptionsForHospitalAdmin(authentication.getName())
        );
    }

    @PatchMapping("/{receptionId}/call")
    @Operation(
            summary = "담당 병원 접수 호출",
            description = "로그인한 병원 운영자의 담당 병원 접수를 호출 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReceptionDto> callReception(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(
                receptionService.callReception(authentication.getName(), receptionId)
        );
    }

    @PatchMapping("/{receptionId}/complete")
    @Operation(
            summary = "담당 병원 접수 완료",
            description = "로그인한 병원 운영자의 담당 병원 접수를 완료 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReceptionDto> completeReception(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(
                receptionService.completeReception(authentication.getName(), receptionId)
        );
    }

    @PatchMapping("/{receptionId}/cancel")
    @Operation(
            summary = "담당 병원 접수 취소",
            description = "로그인한 병원 운영자의 담당 병원 접수를 취소 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReceptionDto> cancelReception(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(
                receptionService.cancelReceptionForHospitalAdmin(authentication.getName(), receptionId)
        );
    }

    @PatchMapping("/{receptionId}/no-show")
    @Operation(
            summary = "담당 병원 접수 노쇼 처리",
            description = "호출 후 방문하지 않은 접수를 노쇼 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReceptionDto> markNoShow(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(
                receptionService.markNoShow(authentication.getName(), receptionId)
        );
    }
}
