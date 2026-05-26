package com.jin.practice.reception.Controller;

import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.VisitType;
import com.jin.practice.reception.service.ReceptionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminReceptionControllerTest {

    @Test
    void adminTodayQueueUsesAuthenticatedAdminsHospital() {
        ReceptionService receptionService = mock(ReceptionService.class);
        AdminReceptionController controller = new AdminReceptionController(receptionService);
        Authentication authentication = new UsernamePasswordAuthenticationToken("admin@example.com", null);
        ReceptionDto reception = receptionDto();

        when(receptionService.getTodayReceptionsForHospitalAdmin("admin@example.com"))
                .thenReturn(List.of(reception));

        ResponseEntity<List<ReceptionDto>> response = controller.getMyHospitalTodayQueue(authentication);

        assertThat(response.getBody()).containsExactly(reception);
    }

    @Test
    void callReceptionUsesAuthenticatedAdminForOwnershipCheck() {
        ReceptionService receptionService = mock(ReceptionService.class);
        AdminReceptionController controller = new AdminReceptionController(receptionService);
        Authentication authentication = new UsernamePasswordAuthenticationToken("admin@example.com", null);
        ReceptionDto reception = receptionDto();

        when(receptionService.callReception("admin@example.com", 1L)).thenReturn(reception);

        ResponseEntity<ReceptionDto> response = controller.callReception(authentication, 1L);

        assertThat(response.getBody()).isEqualTo(reception);
    }

    private ReceptionDto receptionDto() {
        return new ReceptionDto(
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
    }
}
