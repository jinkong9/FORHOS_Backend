package com.jin.practice.reception.dto;

import com.jin.practice.reception.entity.VisitType;

import java.time.LocalDateTime;

public record ReceptionCreateMessage(
        String requestId,
        String email,
        Long hospitalId,
        String patientName,
        VisitType visitType,
        String symptom,
        LocalDateTime requestedAt
) {
    public ReceptionCreateDto toCreateDto() {
        return new ReceptionCreateDto(
                hospitalId,
                patientName,
                visitType,
                symptom
        );
    }
}
