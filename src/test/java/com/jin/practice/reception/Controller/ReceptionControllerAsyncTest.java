package com.jin.practice.reception.Controller;

import com.jin.practice.reception.dto.ReceptionAsyncResponseDto;
import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionCreateMessage;
import com.jin.practice.reception.entity.VisitType;
import com.jin.practice.reception.service.ReceptionCreateProducer;
import com.jin.practice.reception.service.ReceptionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReceptionControllerAsyncTest {

    @Test
    void createReceptionAsyncPublishesMessageAndReturnsAccepted() {
        ReceptionService receptionService = mock(ReceptionService.class);
        ReceptionCreateProducer producer = mock(ReceptionCreateProducer.class);
        ReceptionController controller = new ReceptionController(receptionService, producer);
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", null);
        ReceptionCreateDto request = new ReceptionCreateDto(
                1L,
                "Patient",
                VisitType.FIRST,
                "Headache"
        );
        ArgumentCaptor<ReceptionCreateMessage> messageCaptor =
                ArgumentCaptor.forClass(ReceptionCreateMessage.class);

        ResponseEntity<ReceptionAsyncResponseDto> response =
                controller.createReceptionAsync(authentication, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ACCEPTED");
        assertThat(response.getBody().requestId()).isNotBlank();

        verify(producer).publish(messageCaptor.capture());
        ReceptionCreateMessage message = messageCaptor.getValue();
        assertThat(message.requestId()).isEqualTo(response.getBody().requestId());
        assertThat(message.email()).isEqualTo("user@example.com");
        assertThat(message.hospitalId()).isEqualTo(1L);
        assertThat(message.patientName()).isEqualTo("Patient");
        assertThat(message.visitType()).isEqualTo(VisitType.FIRST);
        assertThat(message.symptom()).isEqualTo("Headache");
        assertThat(message.requestedAt()).isNotNull();
    }
}
