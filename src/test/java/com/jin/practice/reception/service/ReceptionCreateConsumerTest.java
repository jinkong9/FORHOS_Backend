package com.jin.practice.reception.service;

import com.jin.practice.reception.dto.ReceptionCreateMessage;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.VisitType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceptionCreateConsumerTest {

    @Test
    void consumeCreatesReceptionWithExistingService() {
        ReceptionService receptionService = mock(ReceptionService.class);
        ReceptionCreateConsumer consumer = new ReceptionCreateConsumer(receptionService);
        ReceptionCreateMessage message = new ReceptionCreateMessage(
                "request-id",
                "user@example.com",
                1L,
                "Patient",
                VisitType.FIRST,
                "Headache",
                LocalDateTime.now()
        );
        ReceptionDto reception = new ReceptionDto(
                1L,
                1L,
                1L,
                "FORHOS Hospital",
                "Patient",
                VisitType.FIRST,
                "Headache",
                1,
                QueueStatus.WAITING,
                LocalDate.now(),
                LocalDateTime.now(),
                null,
                null,
                null
        );

        when(receptionService.createReception("user@example.com", message.toCreateDto()))
                .thenReturn(reception);

        consumer.consume(message);

        verify(receptionService).createReception("user@example.com", message.toCreateDto());
    }
}
