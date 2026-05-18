package com.jin.practice.reception.Controller;

import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.dto.ReceptionStatusDto;
import com.jin.practice.reception.service.ReceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
@Tag(name = "Reception", description = "접수 및 대기열 API")
public class ReceptionController {
    private final ReceptionService receptionService;

    @PostMapping
    @Operation(
            summary = "접수 생성",
            description = "인증된 회원이 병원 접수를 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReceptionDto> createReception(
            Authentication authentication,
            @Valid @RequestBody ReceptionCreateDto receptionCreateDto
            ) {
        ReceptionDto response = receptionService.createReception(
                authentication.getName(),
                receptionCreateDto
        );

        URI location = URI.create("/api/reception/" + response.id());

        return  ResponseEntity.created(location).body(response);
    }

    @GetMapping("/hospital/{hospitalId}/today")
    @Operation(summary = "오늘 병원 대기열 조회", description = "특정 병원의 오늘 접수 대기열을 조회합니다.")
    public ResponseEntity<List<ReceptionDto>> getTodayQueue(
            @PathVariable Long hospitalId
    ) {
        List<ReceptionDto> response = receptionService.getTodayReceptions(hospitalId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hospital/{receptionId}/status")
    @Operation(
            summary = "접수 상태 조회",
            description = "인증된 회원의 접수 상태와 대기 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReceptionStatusDto> getQueueStatus(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        ReceptionStatusDto response = receptionService.getReceptionStatus(
                authentication.getName(),
                receptionId
        );

        return ResponseEntity.ok(response);
    }
}
