package com.jin.practice.hospital.dto;

import com.jin.practice.hospital.entity.Hospital;
import jakarta.validation.constraints.NotBlank;

public record HospitalDto(
        Long id,
        String name,
        String addr,
        String number,
        boolean openStatus,
        int waitingPeople,
        int waitingTime,
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
