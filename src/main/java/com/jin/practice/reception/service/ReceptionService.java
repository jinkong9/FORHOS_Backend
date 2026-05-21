package com.jin.practice.reception.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.entity.Member;
import com.jin.practice.reception.Repository.ReceptionRepository;
import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.dto.ReceptionStatusDto;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReceptionService {
    private final ReceptionRepository receptionRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;

    @Transactional
    public ReceptionDto createReception(String email, ReceptionCreateDto receptionCreateDto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."));

        Hospital hospital = hospitalRepository.findByIdForUpdate(receptionCreateDto.hospitalId())
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

    public ReceptionStatusDto getReceptionStatus(String email, Long receptionId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."));

        Reception reception = receptionRepository.findById(receptionId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "접수 내역을 찾을 수 없습니다."));

        if(!Objects.equals(reception.getMember().getId(), member.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인의 접수만 조회 가능합니다,"
            );
        }

        int waitingCount = calculateWaitingCount(reception);

        return ReceptionStatusDto.from(reception, waitingCount);
    }

    private int calculateWaitingCount(Reception reception) {
        if (reception.getQueueStatus() != QueueStatus.WAITING) {
            return 0;
        }

        return receptionRepository.findByHospital_IdAndQueueDate(
                        reception.getHospital().getId(),
                        reception.getQueueDate()
                )
                .stream()
                .filter(item -> item.getQueueStatus() == QueueStatus.WAITING)
                .filter(item -> item.getQueueNumber() < reception.getQueueNumber())
                .toList()
                .size();
    }

    public List<ReceptionDto> getMyReceptions(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다"));

        return receptionRepository.findByMember(member)
                .stream()
                .sorted(Comparator.comparing(Reception::getQueueTime).reversed())
                .map(ReceptionDto::from)
                .toList();
    }

    public ReceptionDto cancelReception(String email, Long receptionId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "인증되지 않은 사용자입니다"
                ));

        Reception reception = receptionRepository.findById(receptionId)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "접수 내역을 찾을 수 없습니다."
                ));

        if (!Objects.equals(reception.getMember().getId(), member.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인의 접수만 취소할 수 있습니다."
            );
        }

        if (reception.getQueueStatus() == QueueStatus.CANCELED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 취소된 접수입니다."
            );
        }

        if (reception.getQueueStatus() != QueueStatus.WAITING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "대기 중인 접수만 취소할 수 있습니다."
            );
        }

        reception.cancel();

        return  ReceptionDto.from(receptionRepository.save(reception));
    }

    public ReceptionStatusDto getLatestReceptionStatus(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "인증되지 않은 사용자입니다."
                ));

        Reception reception = receptionRepository.findByMember(member)
                .stream()
                .filter(item ->
                        item.getQueueStatus() == QueueStatus.WAITING ||
                                item.getQueueStatus() == QueueStatus.CALLED
                )
                .max(Comparator.comparing(Reception::getQueueTime))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "진행 중인 접수가 없습니다."
                ));

        int waitingCount = calculateWaitingCount(reception);

        return ReceptionStatusDto.from(reception, waitingCount);
    }

    @Transactional
    public ReceptionDto callReception(Long receptionId) {
        Reception reception = receptionRepository.findById(receptionId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "접수 내역을 찾을 수 없습니다."));

        if(reception.getQueueStatus() != QueueStatus.WAITING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "대기 중인 접수만 호출 가능합니다."
            );
        }

        reception.call();

        return ReceptionDto.from(reception);
    }

    @Transactional
    public ReceptionDto completeReception(Long receptionId) {
        Reception reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "접수 내역을 찾을 수 없습니다."
                ));

        if (reception.getQueueStatus() != QueueStatus.CALLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "호출된 접수만 완료할 수 있습니다."
            );
        }

        reception.complete();

        return ReceptionDto.from(reception);
    }
}
