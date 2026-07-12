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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class ReceptionService {
    private static final int EXPECTED_MINUTES_PER_WAITING_RECEPTION = 10;

    private final ReceptionRepository receptionRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;
    private final QueueNumberGenerator queueNumberGenerator;
    private final ReceptionWaitingQueueStore waitingQueueStore;

    @Autowired
    public ReceptionService(
            ReceptionRepository receptionRepository,
            MemberRepository memberRepository,
            HospitalRepository hospitalRepository,
            QueueNumberGenerator queueNumberGenerator,
            ReceptionWaitingQueueStore waitingQueueStore
    ) {
        this.receptionRepository = receptionRepository;
        this.memberRepository = memberRepository;
        this.hospitalRepository = hospitalRepository;
        this.queueNumberGenerator = queueNumberGenerator;
        this.waitingQueueStore = waitingQueueStore;
    }

    public ReceptionService(
            ReceptionRepository receptionRepository,
            MemberRepository memberRepository,
            HospitalRepository hospitalRepository
    ) {
        this(
                receptionRepository,
                memberRepository,
                hospitalRepository,
                (hospitalId, date) -> receptionRepository.findByHospital_IdAndQueueDate(hospitalId, date)
                        .stream()
                        .mapToInt(Reception::getQueueNumber)
                        .max()
                        .orElse(0) + 1,
                null
        );
    }

    @Transactional
    public ReceptionDto createReception(String email, ReceptionCreateDto receptionCreateDto) {
        Member member = findMemberByEmail(email);

        Hospital hospital = hospitalRepository.findByIdForUpdate(receptionCreateDto.hospitalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "병원을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        validateHospitalAcceptingReception(hospital, today, now.toLocalTime());
        validateNoActiveReception(member, receptionCreateDto.hospitalId(), today);

        List<Reception> todayQueue = receptionRepository.findByHospital_IdAndQueueDate(
                receptionCreateDto.hospitalId(), today);

        int nextQueueNumber = queueNumberGenerator.next(receptionCreateDto.hospitalId(), today);

        Reception reception = new Reception(
                member,
                hospital,
                receptionCreateDto.patientName(),
                receptionCreateDto.visitType(),
                receptionCreateDto.symptom(),
                nextQueueNumber,
                today,
                now
        );

        Reception savedReception = receptionRepository.save(reception);
        addWaitingQueue(savedReception);
        updateHospitalWaitingStats(hospital, appendReception(todayQueue, savedReception));

        return ReceptionDto.from(savedReception);
    }

    private void validateHospitalAcceptingReception(Hospital hospital, LocalDate date, LocalTime time) {
        if (!hospital.acceptsReception(date, time)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "현재 접수 가능한 시간이 아닙니다."
            );
        }
    }

    private void validateNoActiveReception(Member member, Long hospitalId, LocalDate queueDate) {
        boolean hasActiveReception = receptionRepository.existsByMemberAndHospital_IdAndQueueDateAndQueueStatusIn(
                member,
                hospitalId,
                queueDate,
                List.of(QueueStatus.WAITING, QueueStatus.CALLED)
        );

        if (hasActiveReception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 진행 중인 접수가 있습니다."
            );
        }
    }

    public List<ReceptionDto> getTodayReceptions(Long hospitalId) {
        LocalDate today = LocalDate.now();

        return findTodayWaitingReceptions(hospitalId, today)
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

    public Page<ReceptionDto> getReceptionsForHospitalAdmin(
            String email,
            LocalDate queueDate,
            QueueStatus queueStatus,
            Pageable pageable
    ) {
        Member member = findMemberByEmail(email);
        LocalDate targetDate = queueDate == null ? LocalDate.now() : queueDate;

        Page<Reception> receptions;
        if (member.getRole() == MemberRole.ADMIN) {
            receptions = findAdminReceptions(targetDate, queueStatus, pageable);
        } else {
            Hospital hospital = getManageableHospital(member);
            receptions = findHospitalAdminReceptions(hospital.getId(), targetDate, queueStatus, pageable);
        }

        return receptions.map(ReceptionDto::from);
    }

    private Page<Reception> findAdminReceptions(
            LocalDate queueDate,
            QueueStatus queueStatus,
            Pageable pageable
    ) {
        if (queueStatus == null) {
            return receptionRepository.findByQueueDate(queueDate, pageable);
        }

        return receptionRepository.findByQueueDateAndQueueStatus(queueDate, queueStatus, pageable);
    }

    private Page<Reception> findHospitalAdminReceptions(
            Long hospitalId,
            LocalDate queueDate,
            QueueStatus queueStatus,
            Pageable pageable
    ) {
        if (queueStatus == null) {
            return receptionRepository.findByHospital_IdAndQueueDate(hospitalId, queueDate, pageable);
        }

        return receptionRepository.findByHospital_IdAndQueueDateAndQueueStatus(
                hospitalId,
                queueDate,
                queueStatus,
                pageable
        );
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

        if (waitingQueueStore != null) {
            return waitingQueueStore.countWaitingBefore(reception)
                    .orElseGet(() -> calculateWaitingCountFromDatabase(reception));
        }

        return calculateWaitingCountFromDatabase(reception);
    }

    private int calculateWaitingCountFromDatabase(Reception reception) {
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
        removeWaitingQueue(reception);
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
        removeWaitingQueue(reception);
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
        removeWaitingQueue(reception);
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
        removeWaitingQueue(reception);
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
        removeWaitingQueue(reception);
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

    private List<Reception> findTodayWaitingReceptions(Long hospitalId, LocalDate today) {
        if (waitingQueueStore != null) {
            return waitingQueueStore.findWaitingReceptionIds(hospitalId, today)
                    .map(receptionIds -> findReceptionsByIdsOrFallback(hospitalId, today, receptionIds))
                    .orElseGet(() -> findTodayWaitingReceptionsFromDatabaseAndWarmRedis(hospitalId, today));
        }

        return findTodayWaitingReceptionsFromDatabase(hospitalId, today);
    }

    private List<Reception> findReceptionsByIdsOrFallback(
            Long hospitalId,
            LocalDate today,
            List<Long> receptionIds
    ) {
        List<Reception> receptions = findReceptionsByIdsPreservingOrder(receptionIds);

        if (!receptionIds.isEmpty() && receptions.size() != receptionIds.size()) {
            return findTodayWaitingReceptionsFromDatabaseAndWarmRedis(hospitalId, today);
        }

        return receptions;
    }

    private List<Reception> findReceptionsByIdsPreservingOrder(List<Long> receptionIds) {
        Map<Long, Reception> receptionsById = new HashMap<>();
        receptionRepository.findAllById(receptionIds)
                .forEach(reception -> receptionsById.put(reception.getId(), reception));

        return receptionIds.stream()
                .map(receptionsById::get)
                .filter(Objects::nonNull)
                .filter(reception -> reception.getQueueStatus() == QueueStatus.WAITING)
                .toList();
    }

    private List<Reception> findTodayWaitingReceptionsFromDatabaseAndWarmRedis(Long hospitalId, LocalDate today) {
        List<Reception> receptions = findTodayWaitingReceptionsFromDatabase(hospitalId, today);
        warmWaitingQueue(hospitalId, today, receptions);
        return receptions;
    }

    private List<Reception> findTodayWaitingReceptionsFromDatabase(Long hospitalId, LocalDate today) {
        return receptionRepository.findByHospital_IdAndQueueDate(hospitalId, today)
                .stream()
                .filter(reception -> reception.getQueueStatus() == QueueStatus.WAITING)
                .sorted(Comparator.comparingInt(Reception::getQueueNumber))
                .toList();
    }

    private void addWaitingQueue(Reception reception) {
        if (waitingQueueStore != null) {
            waitingQueueStore.addWaiting(reception);
        }
    }

    private void removeWaitingQueue(Reception reception) {
        if (waitingQueueStore != null) {
            waitingQueueStore.remove(reception);
        }
    }

    private void warmWaitingQueue(Long hospitalId, LocalDate date, List<Reception> receptions) {
        if (waitingQueueStore != null) {
            waitingQueueStore.replaceWaitingQueue(hospitalId, date, receptions);
        }
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
