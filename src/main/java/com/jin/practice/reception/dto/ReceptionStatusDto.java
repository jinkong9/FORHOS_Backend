package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "접수 상태 응답")
public record ReceptionStatusDto(
        @Schema(description = "접수 ID", example = "1")
        Long receptionId,
        @Schema(description = "병원 ID", example = "1")
        Long hospitalId,
        @Schema(description = "병원명", example = "FORHOS 병원")
        String hospitalName,
        @Schema(description = "접수 상태", example = "WAITING", allowableValues = {"WAITING", "CALLED", "COMPLETED", "CANCELED"})
        QueueStatus status,
        @Schema(description = "대기 번호", example = "7")
        int queueNumber,
        @Schema(description = "앞 대기 인원", example = "3")
        int waitingCount
) {
    public static ReceptionStatusDto from(Reception reception, int waitingCount) {
        return new ReceptionStatusDto(
                reception.getId(),
                reception.getHospital().getId(),
                reception.getHospital().getName(),
                reception.getQueueStatus(),
                reception.getQueueNumber(),
                waitingCount
        );
    }
}
