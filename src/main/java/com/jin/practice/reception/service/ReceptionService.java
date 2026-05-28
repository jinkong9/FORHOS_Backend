package com.jin.practice.reception.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.entity.Member;
import com.jin.practice.member.entity.MemberRole;
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
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReceptionService {
    private static final int EXPECTED_MINUTES_PER_WAITING_RECEPTION = 10;

    private final ReceptionRepository receptionRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;

    @Transactional
    public ReceptionDto createReception(String email, ReceptionCreateDto receptionCreateDto) {
        Member member = findMemberByEmail(email);

        Hospital hospital = hospitalRepository.findByIdForUpdate(receptionCreateDto.hospitalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "병원을 찾을 수 없습니다."));

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
        updateHospitalWaitingStats(hospital, appendReception(todayQueue, savedReception));

        return ReceptionDto.from(savedReception);
    }

    public List<ReceptionDto> getTodayReceptions(Long hospitalId) {
        LocalDate today = LocalDate.now();

        return receptionRepository.findByHospital_IdAndQueueDate(hospitalId, today)
                .stream()
                .sorted(Comparator.comparingInt(Reception::getQueueNumber))
                .map(ReceptionDto::from)
                .toList();
    }

    public List<ReceptionDto> getTodayReceptionsForHospitalAdmin(String email) {
        Member member = findMemberByEmail(email);

        if (member.getRole() == MemberRole.ADMIN) {
            return receptionRepository.findByQueueDate(LocalDate.now())
                    .stream()
                    .sorted(Comparator
                            .comparing((Reception reception) -> reception.getHospital().getId())
                            .thenComparingInt(Reception::getQueueNumber))
                    .map(ReceptionDto::from)
                    .toList();
        }

        Hospital hospital = getManageableHospital(member);

        return getTodayReceptions(hospital.getId());
    }

    public ReceptionStatusDto getReceptionStatus(String email, Long receptionId) {
        Member member = findMemberByEmail(email);
        Reception reception = findReceptionById(receptionId);

        if (!Objects.equals(reception.getMember().getId(), member.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인의 접수만 조회할 수 있습니다."
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
        Member member = findMemberByEmail(email);

        return receptionRepository.findByMember(member)
                .stream()
                .sorted(Comparator.comparing(Reception::getQueueTime).reversed())
                .map(ReceptionDto::from)
                .toList();
    }

    @Transactional
    public ReceptionDto cancelReception(String email, Long receptionId) {
        Member member = findMemberByEmail(email);
        Reception reception = findReceptionById(receptionId);

        if (!Objects.equals(reception.getMember().getId(), member.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인의 접수만 취소할 수 있습니다."
            );
        }

        validateCancelableByUser(reception);
        reception.cancel();
        refreshHospitalWaitingStats(reception.getHospital());

        return ReceptionDto.from(reception);
    }

    @Transactional
    public ReceptionDto cancelReceptionForHospitalAdmin(String email, Long receptionId) {
        Member member = findMemberByEmail(email);
        Reception reception = findReceptionById(receptionId);

        validateHospitalAdminCanManage(member, reception);

        if (reception.getQueueStatus() == QueueStatus.CANCELED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 취소된 접수입니다."
            );
        }

        if (reception.getQueueStatus() == QueueStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "완료된 접수는 취소할 수 없습니다."
            );
        }

        reception.cancel();
        refreshHospitalWaitingStats(reception.getHospital());

        return ReceptionDto.from(reception);
    }

    public ReceptionStatusDto getLatestReceptionStatus(String email) {
        Member member = findMemberByEmail(email);

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
    public ReceptionDto callReception(String email, Long receptionId) {
        Member member = findMemberByEmail(email);
        Reception reception = findReceptionById(receptionId);

        validateHospitalAdminCanManage(member, reception);

        if (reception.getQueueStatus() != QueueStatus.WAITING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "대기 중인 접수만 호출 가능합니다."
            );
        }

        reception.call();
        refreshHospitalWaitingStats(reception.getHospital());

        return ReceptionDto.from(reception);
    }

    @Transactional
    public ReceptionDto completeReception(String email, Long receptionId) {
        Member member = findMemberByEmail(email);
        Reception reception = findReceptionById(receptionId);

        validateHospitalAdminCanManage(member, reception);

        if (reception.getQueueStatus() != QueueStatus.CALLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "호출된 접수만 완료할 수 있습니다."
            );
        }

        reception.complete();
        refreshHospitalWaitingStats(reception.getHospital());

        return ReceptionDto.from(reception);
    }

    @Transactional
    public ReceptionDto markNoShow(String email, Long receptionId) {
        Member member = findMemberByEmail(email);
        Reception reception = findReceptionById(receptionId);

        validateHospitalAdminCanManage(member, reception);

        if (reception.getQueueStatus() != QueueStatus.CALLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "호출된 접수만 노쇼 처리할 수 있습니다."
            );
        }

        reception.markNoShow();
        refreshHospitalWaitingStats(reception.getHospital());

        return ReceptionDto.from(reception);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "인증되지 않은 사용자입니다."
                ));
    }

    private Reception findReceptionById(Long receptionId) {
        return receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "접수 내역을 찾을 수 없습니다."
                ));
    }

    private Hospital getManageableHospital(Member member) {
        if (member.getRole() != MemberRole.HOSPITAL_ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "병원 운영자만 이용할 수 있습니다."
            );
        }

        if (member.getHospital() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "담당 병원이 지정되지 않았습니다."
            );
        }

        return member.getHospital();
    }

    private void validateHospitalAdminCanManage(Member member, Reception reception) {
        if (member.getRole() == MemberRole.ADMIN) {
            return;
        }

        Hospital hospital = getManageableHospital(member);

        if (!Objects.equals(hospital.getId(), reception.getHospital().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "담당 병원의 접수만 처리할 수 있습니다."
            );
        }
    }

    private void validateCancelableByUser(Reception reception) {
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
    }

    private List<Reception> appendReception(List<Reception> receptions, Reception reception) {
        return Stream.concat(receptions.stream(), Stream.of(reception))
                .toList();
    }

    private void refreshHospitalWaitingStats(Hospital hospital) {
        List<Reception> todayQueue = receptionRepository.findByHospital_IdAndQueueDate(
                hospital.getId(),
                LocalDate.now()
        );

        updateHospitalWaitingStats(hospital, todayQueue);
    }

    private void updateHospitalWaitingStats(Hospital hospital, List<Reception> todayQueue) {
        int waitingPeople = (int) todayQueue.stream()
                .filter(reception -> reception.getQueueStatus() == QueueStatus.WAITING)
                .count();
        int expectedMinutes = calculateExpectedMinutesPerWaitingReception(hospital.getId());

        hospital.updateWaitingStats(
                waitingPeople,
                waitingPeople * expectedMinutes
        );
    }

    private int calculateExpectedMinutesPerWaitingReception(Long hospitalId) {
        List<Reception> completedReceptions =
                receptionRepository.findTop10ByHospital_IdAndQueueStatusAndCalledTimeIsNotNullAndDoneTimeIsNotNullOrderByDoneTimeDesc(
                        hospitalId,
                        QueueStatus.COMPLETED
                );

        double averageMinutes = completedReceptions.stream()
                .mapToLong(reception -> ChronoUnit.MINUTES.between(
                        reception.getCalledTime(),
                        reception.getDoneTime()
                ))
                .filter(minutes -> minutes > 0)
                .average()
                .orElse(EXPECTED_MINUTES_PER_WAITING_RECEPTION);

        return Math.max(1, (int) Math.round(averageMinutes));
    }
}
