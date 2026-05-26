package com.jin.practice.reception.Controller;

import com.jin.practice.common.ErrorResponse;
import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.dto.ReceptionStatusDto;
import com.jin.practice.reception.service.ReceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            description = "인증된 회원의 병원 접수를 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "접수 생성 성공",
                    content = @Content(schema = @Schema(implementation = ReceptionDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReceptionDto> createReception(
            Authentication authentication,
            @Valid @RequestBody ReceptionCreateDto receptionCreateDto
    ) {
        ReceptionDto response = receptionService.createReception(
                authentication.getName(),
                receptionCreateDto
        );

        URI location = URI.create("/api/reception/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/hospital/{hospitalId}/today")
    @Operation(summary = "오늘 병원 대기열 조회", description = "특정 병원의 오늘 접수 대기열을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "병원별 오늘 접수 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReceptionDto.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "접수 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReceptionStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 접수가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "접수 내역을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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

    @GetMapping("/me")
    @Operation(
            summary = "내 접수 내역 조회",
            description = "인증된 회원의 접수 내역을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 접수 내역 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReceptionDto.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<ReceptionDto>> getMyReceptions(
            Authentication authentication
    ) {
        return ResponseEntity.ok(receptionService.getMyReceptions(authentication.getName()));
    }

    @GetMapping("/me/latest")
    @Operation(
            summary = "내 최신 진행 중 접수 상태 조회",
            description = "인증된 회원의 가장 최근 진행 중인 접수 상태와 앞 대기 인원을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "최신 진행 중 접수 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReceptionStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "진행 중인 접수가 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReceptionStatusDto> getLatestReceptionStatus(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                receptionService.getLatestReceptionStatus(authentication.getName())
        );
    }

    @PatchMapping("/{receptionId}/cancel")
    @Operation(
            summary = "내 접수 취소",
            description = "인증된 회원의 대기 중인 접수를 취소합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "접수 취소 성공",
                    content = @Content(schema = @Schema(implementation = ReceptionDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 접수가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "접수 내역을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 취소되었거나 취소할 수 없는 상태",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReceptionDto> cancelReception(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(
                receptionService.cancelReception(authentication.getName(), receptionId)
        );
    }

    @PatchMapping("/{receptionId}/call")
    @Operation(
            summary = "접수 호출",
            description = "대기 중인 접수를 호출 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "접수 호출 성공",
                    content = @Content(schema = @Schema(implementation = ReceptionDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "접수 내역을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "대기 중인 접수가 아니라 호출할 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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
            summary = "접수 완료",
            description = "호출된 접수를 완료 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "접수 완료 성공",
                    content = @Content(schema = @Schema(implementation = ReceptionDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 또는 토큰 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "접수 내역을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "호출된 접수가 아니라 완료할 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReceptionDto> completeReception(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(
                receptionService.completeReception(authentication.getName(), receptionId)
        );
    }
}
