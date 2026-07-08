package com.jin.practice.reception.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.Repository.MemberRepository;
import com.jin.practice.member.entity.Member;
import com.jin.practice.member.entity.MemberRole;
import com.jin.practice.reception.Repository.ReceptionRepository;
import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import com.jin.practice.reception.entity.VisitType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceptionServiceTest {

    @Test
    void callReceptionChangesWaitingReceptionToCalled() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Reception reception = waitingReception();
        Member admin = hospitalAdminWithHospital(1L);

        when(receptionRepository.findById(1L)).thenReturn(Optional.of(reception));
        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        ReceptionDto response = receptionService.callReception("admin@example.com", 1L);

        assertThat(response.queueStatus()).isEqualTo(QueueStatus.CALLED);
        assertThat(response.calledTime()).isNotNull();
    }

    @Test
    void hospitalAdminCannotCallReceptionForOtherHospital() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Reception reception = waitingReception();
        Member otherHospitalAdmin = hospitalAdminWithHospital(2L);

        when(receptionRepository.findById(1L)).thenReturn(Optional.of(reception));
        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(otherHospitalAdmin));

        assertThatThrownBy(() -> receptionService.callReception("admin@example.com", 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanCallReceptionForAnyHospital() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Reception reception = waitingReception();
        Member admin = memberWithRole(MemberRole.ADMIN);

        when(receptionRepository.findById(1L)).thenReturn(Optional.of(reception));
        when(memberRepository.findByEmail("root@example.com")).thenReturn(Optional.of(admin));

        ReceptionDto response = receptionService.callReception("root@example.com", 1L);

        assertThat(response.queueStatus()).isEqualTo(QueueStatus.CALLED);
    }

    @Test
    void getTodayReceptionsForHospitalAdminReturnsOnlyOwnHospitalQueue() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = hospitalAdminWithHospital(1L);
        Reception reception = waitingReception();

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findByHospital_IdAndQueueDate(1L, LocalDate.now()))
                .thenReturn(List.of(reception));

        List<ReceptionDto> response = receptionService.getTodayReceptionsForHospitalAdmin("admin@example.com");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().hospitalId()).isEqualTo(1L);
    }

    @Test
    void adminRoleCanReadAllTodayQueues() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = memberWithRole(MemberRole.ADMIN);
        Reception reception = waitingReception();

        when(memberRepository.findByEmail("root@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findByQueueDate(LocalDate.now())).thenReturn(List.of(reception));

        List<ReceptionDto> response = receptionService.getTodayReceptionsForHospitalAdmin("root@example.com");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().hospitalId()).isEqualTo(1L);
    }

    @Test
    void hospitalAdminReceptionSearchReturnsOwnHospitalQueueWithDateAndStatus() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = hospitalAdminWithHospital(1L);
        Reception reception = waitingReception();
        LocalDate date = LocalDate.of(2026, 7, 8);
        PageRequest pageable = PageRequest.of(0, 20);

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findByHospital_IdAndQueueDateAndQueueStatus(
                1L,
                date,
                QueueStatus.WAITING,
                pageable
        )).thenReturn(new PageImpl<>(List.of(reception), pageable, 1));

        Page<ReceptionDto> response = receptionService.getReceptionsForHospitalAdmin(
                "admin@example.com",
                date,
                QueueStatus.WAITING,
                pageable
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().hospitalId()).isEqualTo(1L);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void adminReceptionSearchReturnsAllHospitalsAndDefaultsToToday() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = memberWithRole(MemberRole.ADMIN);
        Reception reception = waitingReception();
        PageRequest pageable = PageRequest.of(0, 20);

        when(memberRepository.findByEmail("root@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findByQueueDate(LocalDate.now(), pageable))
                .thenReturn(new PageImpl<>(List.of(reception), pageable, 1));

        Page<ReceptionDto> response = receptionService.getReceptionsForHospitalAdmin(
                "root@example.com",
                null,
                null,
                pageable
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().hospitalId()).isEqualTo(1L);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void createReceptionUpdatesHospitalWaitingStats() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, hospitalRepository);
        Member member = memberWithRole(MemberRole.USER);
        Hospital hospital = hospitalWithId(1L);
        Reception existingWaitingReception = waitingReception();
        Reception savedReception = new Reception(
                member,
                hospital,
                "Patient",
                VisitType.FIRST,
                "Headache",
                2,
                LocalDate.now(),
                LocalDateTime.now()
        );

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(hospitalRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(hospital));
        when(receptionRepository.findByHospital_IdAndQueueDate(1L, LocalDate.now()))
                .thenReturn(List.of(existingWaitingReception));
        when(receptionRepository.save(any(Reception.class))).thenReturn(savedReception);

        receptionService.createReception("user@example.com", new ReceptionCreateDto(
                1L,
                "Patient",
                VisitType.FIRST,
                "Headache"
        ));

        assertThat(hospital.getWaitingPeople()).isEqualTo(2);
        assertThat(hospital.getWaitingTime()).isEqualTo(20);
    }

    @Test
    void createReceptionRejectsDuplicateActiveReceptionForSameHospitalToday() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, hospitalRepository);
        Member member = memberWithRole(MemberRole.USER);
        Hospital hospital = hospitalWithId(1L);

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(hospitalRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(hospital));
        when(receptionRepository.existsByMemberAndHospital_IdAndQueueDateAndQueueStatusIn(
                member,
                1L,
                LocalDate.now(),
                List.of(QueueStatus.WAITING, QueueStatus.CALLED)
        )).thenReturn(true);

        assertThatThrownBy(() -> receptionService.createReception("user@example.com", new ReceptionCreateDto(
                1L,
                "Patient",
                VisitType.FIRST,
                "Headache"
        )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(receptionRepository, never()).save(any(Reception.class));
    }

    @Test
    void createReceptionRejectsClosedHospital() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, hospitalRepository);
        Member member = memberWithRole(MemberRole.USER);
        Hospital hospital = hospitalWithId(1L);
        ReflectionTestUtils.setField(hospital, "openStatus", false);

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(hospitalRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(hospital));

        assertThatThrownBy(() -> receptionService.createReception("user@example.com", new ReceptionCreateDto(
                1L,
                "Patient",
                VisitType.FIRST,
                "Headache"
        )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(receptionRepository, never()).save(any(Reception.class));
    }

    @Test
    void callReceptionRefreshesHospitalWaitingStats() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = hospitalAdminWithHospital(1L);
        Reception calledReception = waitingReception();
        Reception remainingWaitingReception = waitingReceptionWithIdAndQueueNumber(2L, 2);
        Hospital hospital = calledReception.getHospital();

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findById(1L)).thenReturn(Optional.of(calledReception));
        when(receptionRepository.findByHospital_IdAndQueueDate(1L, LocalDate.now()))
                .thenReturn(List.of(calledReception, remainingWaitingReception));

        receptionService.callReception("admin@example.com", 1L);

        assertThat(hospital.getWaitingPeople()).isEqualTo(1);
        assertThat(hospital.getWaitingTime()).isEqualTo(10);
    }

    @Test
    void waitTimeUsesAverageCompletedReceptionDuration() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = hospitalAdminWithHospital(1L);
        Reception calledReception = waitingReception();
        Reception firstWaitingReception = waitingReceptionWithIdAndQueueNumber(2L, 2);
        Reception secondWaitingReception = waitingReceptionWithIdAndQueueNumber(3L, 3);
        Reception completedReception = completedReceptionWithDuration(20);
        Hospital hospital = calledReception.getHospital();

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findById(1L)).thenReturn(Optional.of(calledReception));
        when(receptionRepository.findByHospital_IdAndQueueDate(1L, LocalDate.now()))
                .thenReturn(List.of(calledReception, firstWaitingReception, secondWaitingReception));
        when(receptionRepository.findTop10ByHospital_IdAndQueueStatusAndCalledTimeIsNotNullAndDoneTimeIsNotNullOrderByDoneTimeDesc(
                1L,
                QueueStatus.COMPLETED
        )).thenReturn(List.of(completedReception));

        receptionService.callReception("admin@example.com", 1L);

        assertThat(hospital.getWaitingPeople()).isEqualTo(2);
        assertThat(hospital.getWaitingTime()).isEqualTo(40);
    }

    @Test
    void markNoShowChangesCalledReceptionToNoShowAndRefreshesStats() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, memberRepository, null);
        Member admin = hospitalAdminWithHospital(1L);
        Reception reception = waitingReception();
        reception.call();

        when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(receptionRepository.findById(1L)).thenReturn(Optional.of(reception));
        when(receptionRepository.findByHospital_IdAndQueueDate(1L, LocalDate.now()))
                .thenReturn(List.of(reception));

        ReceptionDto response = receptionService.markNoShow("admin@example.com", 1L);

        assertThat(response.queueStatus()).isEqualTo(QueueStatus.NO_SHOW);
        assertThat(reception.getNoShowTime()).isNotNull();
        assertThat(reception.getHospital().getWaitingPeople()).isZero();
    }

    private Reception waitingReception() {
        Member member = memberWithRole(MemberRole.USER);
        Hospital hospital = hospitalWithId(1L);

        Reception reception = new Reception(
                member,
                hospital,
                "Patient",
                VisitType.FIRST,
                "Headache",
                1,
                LocalDate.now(),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(reception, "id", 1L);
        return reception;
    }

    private Reception waitingReceptionWithIdAndQueueNumber(Long id, int queueNumber) {
        Reception reception = new Reception(
                memberWithRole(MemberRole.USER),
                hospitalWithId(1L),
                "Patient",
                VisitType.FIRST,
                "Headache",
                queueNumber,
                LocalDate.now(),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(reception, "id", id);
        return reception;
    }

    private Reception completedReceptionWithDuration(int minutes) {
        Reception reception = waitingReceptionWithIdAndQueueNumber(99L, 99);
        LocalDateTime calledTime = LocalDateTime.now().minusMinutes(minutes + 1L);
        ReflectionTestUtils.setField(reception, "queueStatus", QueueStatus.COMPLETED);
        ReflectionTestUtils.setField(reception, "calledTime", calledTime);
        ReflectionTestUtils.setField(reception, "doneTime", calledTime.plusMinutes(minutes));
        return reception;
    }

    private Member hospitalAdminWithHospital(Long hospitalId) {
        Member member = memberWithRole(MemberRole.HOSPITAL_ADMIN);
        ReflectionTestUtils.setField(member, "hospital", hospitalWithId(hospitalId));
        return member;
    }

    private Member memberWithRole(MemberRole role) {
        Member member = new Member(
                "user@example.com",
                "encoded-password",
                "User",
                30,
                "010-1234-5678",
                "MALE",
                "Seoul",
                "none",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "role", role);
        return member;
    }

    private Hospital hospitalWithId(Long id) {
        Hospital hospital = new Hospital();
        ReflectionTestUtils.setField(hospital, "id", id);
        ReflectionTestUtils.setField(hospital, "name", "FORHOS Hospital");
        return hospital;
    }
}
