package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import com.jin.practice.reception.entity.VisitType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceptionDto(
        Long id,
        Long memberId,
        Long hospitalId,
        String hospitalName,
        String patientName,
        VisitType visitType,
        String symptom,
        int queueNumber,
        QueueStatus queueStatus,
        LocalDate queueDate,
        LocalDateTime queueTime,
        LocalDateTime calledTime,
        LocalDateTime doneTime,
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
