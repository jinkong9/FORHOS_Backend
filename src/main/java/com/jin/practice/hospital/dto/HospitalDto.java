package com.jin.practice.hospital.dto;

import com.jin.practice.hospital.entity.Hospital;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "병원 응답")
public record HospitalDto(
        @Schema(description = "병원 ID", example = "1")
        Long id,
        @Schema(description = "병원명", example = "FORHOS 병원")
        String name,
        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
        String addr,
        @Schema(description = "대표 전화번호", example = "02-1234-5678")
        String number,
        @Schema(description = "운영 여부", example = "true")
        boolean openStatus,
        @Schema(description = "현재 대기 인원", example = "5")
        int waitingPeople,
        @Schema(description = "예상 대기 시간(분)", example = "30")
        int waitingTime,
        @Schema(description = "평점", example = "4.5")
        double rating
) {
    public static HospitalDto from(Hospital hospital) {
        return new HospitalDto(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddr(),
                hospital.getNumber(),
                hospital.isOpenStatus(),
                hospital.getWaitingPeople(),
                hospital.getWaitingTime(),
                hospital.getRating()
        );
    }
}
