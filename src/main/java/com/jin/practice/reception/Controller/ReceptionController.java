package com.jin.practice.reception.Controller;

import com.jin.practice.reception.dto.ReceptionCreateDto;
import com.jin.practice.reception.dto.ReceptionDto;
import com.jin.practice.reception.dto.ReceptionStatusDto;
import com.jin.practice.reception.service.ReceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
public class ReceptionController {
    private final ReceptionService receptionService;

    @PostMapping
    public ResponseEntity<ReceptionDto> createReception(
            Authentication authentication,
            @Valid @RequestBody ReceptionCreateDto receptionCreateDto
            ) {
        ReceptionDto response = receptionService.createReception(
                authentication.getName(),
                receptionCreateDto
        );

        URI location = URI.create("/api/reception/" + response.id());

        return  ResponseEntity.created(location).body(response);
    }

    @GetMapping("/hospital/{hospitalId}/today")
    public ResponseEntity<List<ReceptionDto>> getTodayQueue(
            @PathVariable Long hospitalId
    ) {
        List<ReceptionDto> response = receptionService.getTodayReceptions(hospitalId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hospital/{receptionId}/status")
    public ResponseEntity<ReceptionStatusDto> getQueueStatus(
            Authentication authentication,
            @PathVariable Long receptionId
    ) {
        ReceptionStatusDto response = receptionService.getReceptionStatus(
                authentication.getName(),
                receptionId
        );

        return ResponseEntity.ok(response);
    }
}
