package com.jin.practice.reception.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.entity.Member;
import com.jin.practice.reception.Repository.ReceptionRepository;
import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.entity.Reception;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceptionService {
    private final ReceptionRepository receptionRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;

    public ReceptionDto createReception(String email, ReceptionCreateDto receptionCreateDto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."));

        Hospital hospital = hospitalRepository.findById(receptionCreateDto.hospitalId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "병원을 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        List<Reception> todayQueue = receptionRepository.findByHospital_IdAndQueueDate(
                receptionCreateDto.hospitalId(), today);

        int nextQueueNumber = todayQueue.stream()
                .mapToInt(Reception::getQueueNumber)
                .max()
                .orElse(0) + 1;

        Reception reception = new Reception(
                member,
                hospital,
                receptionCreateDto.patientName(),
                receptionCreateDto.visitType(),
                receptionCreateDto.symptom(),
                nextQueueNumber,
                today,
                LocalDateTime.now()
        );

        Reception savedReception = receptionRepository.save(reception);

        return ReceptionDto.from(savedReception);
    }

    public List<ReceptionDto> getTodayReceptions(Long hospitalId) {
        LocalDate today =  LocalDate.now();

        return receptionRepository.findByHospital_IdAndQueueDate(hospitalId, today)
                .stream()
                .sorted(Comparator.comparingInt(Reception::getQueueNumber))
                .map(ReceptionDto::from)
                .toList();
    }

}
