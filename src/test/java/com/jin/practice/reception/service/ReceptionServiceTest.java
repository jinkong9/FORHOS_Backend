package com.jin.practice.reception.service;

import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.entity.Member;
import com.jin.practice.reception.Repository.ReceptionRepository;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import com.jin.practice.reception.entity.VisitType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReceptionServiceTest {

    @Test
    void callReceptionChangesWaitingReceptionToCalled() {
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        ReceptionService receptionService = new ReceptionService(receptionRepository, null, null);
        Reception reception = waitingReception();

        when(receptionRepository.findById(1L)).thenReturn(Optional.of(reception));

        ReceptionDto response = receptionService.callReception(1L);

        assertThat(response.queueStatus()).isEqualTo(QueueStatus.CALLED);
        assertThat(response.calledTime()).isNotNull();
    }

    private Reception waitingReception() {
        Member member = new Member(
                "user@example.com",
                "encoded-password",
                "홍길동",
                30,
                "010-1234-5678",
                "MALE",
                "Seoul",
                "none",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(member, "id", 1L);

        Hospital hospital = new Hospital();
        ReflectionTestUtils.setField(hospital, "id", 1L);
        ReflectionTestUtils.setField(hospital, "name", "FORHOS 병원");

        Reception reception = new Reception(
                member,
                hospital,
                "홍길동",
                VisitType.FIRST,
                "기침",
                1,
                LocalDate.now(),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(reception, "id", 1L);
        return reception;
    }
}
