package com.jin.practice.hospital.Controller;

import com.jin.practice.common.ErrorResponse;
import com.jin.practice.hospital.dto.HospitalDto;
import com.jin.practice.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
@Tag(name = "Hospital", description = "병원 API")
public class HospitalController {
    private final HospitalService hospitalService;

    @GetMapping
    @Operation(summary = "병원 목록 조회", description = "등록된 병원 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "병원 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HospitalDto.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<HospitalDto>> list() {
        List<HospitalDto> response = hospitalService.findAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hospitalId}")
    @Operation(summary = "병원 상세 조회", description = "병원 ID로 병원 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "병원 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = HospitalDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<HospitalDto> detail(
            @PathVariable  Long hospitalId
    ) {
        HospitalDto response = hospitalService.findById(hospitalId);
        return ResponseEntity.ok(response);
    }
}
