package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import com.jin.practice.reception.entity.VisitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "접수 응답")
public record ReceptionDto(
        @Schema(description = "접수 ID", example = "1")
        Long id,
        @Schema(description = "회원 ID", example = "1")
        Long memberId,
        @Schema(description = "병원 ID", example = "1")
        Long hospitalId,
        @Schema(description = "병원명", example = "FORHOS 병원")
        String hospitalName,
        @Schema(description = "환자 이름", example = "홍길동")
        String patientName,
        @Schema(description = "방문 유형", example = "FIRST", allowableValues = {"FIRST", "RETURN"})
        VisitType visitType,
        @Schema(description = "증상", example = "기침과 발열")
        String symptom,
        @Schema(description = "대기 번호", example = "7")
        int queueNumber,
        @Schema(description = "대기 상태", example = "WAITING", allowableValues = {"WAITING", "CALLED", "COMPLETED", "CANCELED"})
        QueueStatus queueStatus,
        @Schema(description = "대기 날짜", example = "2026-05-18")
        LocalDate queueDate,
        @Schema(description = "접수 시간", example = "2026-05-18T10:15:30")
        LocalDateTime queueTime,
        @Schema(description = "호출 시간", example = "2026-05-18T10:30:00")
        LocalDateTime calledTime,
        @Schema(description = "완료 시간", example = "2026-05-18T10:45:00")
        LocalDateTime doneTime,
        @Schema(description = "취소 시간", example = "2026-05-18T10:20:00")
        LocalDateTime canceledTime
) {
    public static ReceptionDto from(Reception reception) {
        return new ReceptionDto(
                reception.getId(),
                reception.getMember().getId(),
                reception.getHospital().getId(),
                reception.getHospital().getName(),
                reception.getPatientName(),
                reception.getVisitType(),
                reception.getSymptom(),
                reception.getQueueNumber(),
                reception.getQueueStatus(),
                reception.getQueueDate(),
                reception.getQueueTime(),
                reception.getCalledTime(),
                reception.getDoneTime(),
                reception.getCanceledTime()
        );
    }
}
