package com.jin.practice.hospital.dto;

import com.jin.practice.hospital.entity.Hospital;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

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
        @Schema(description = "운영 시작 시간", example = "09:00")
        LocalTime openTime,
        @Schema(description = "운영 종료 시간", example = "18:00")
        LocalTime closeTime,
        @Schema(description = "점심 시작 시간", example = "12:30")
        LocalTime lunchStartTime,
        @Schema(description = "점심 종료 시간", example = "13:30")
        LocalTime lunchEndTime,
        @Schema(description = "휴무 요일 목록", example = "SUNDAY,SATURDAY")
        String closedDays,
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
                hospital.getOpenTime(),
                hospital.getCloseTime(),
                hospital.getLunchStartTime(),
                hospital.getLunchEndTime(),
                hospital.getClosedDays(),
                hospital.getWaitingPeople(),
                hospital.getWaitingTime(),
                hospital.getRating()
        );
    }
}
