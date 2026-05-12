package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;

public record ReceptionStatusDto(
        Long receptionId,
        Long hospitalId,
        String hospitalName,
        QueueStatus status,
        int queueNumber,
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
