package com.jin.practice.hospital.Controller;

import com.jin.practice.hospital.dto.HospitalDto;
import com.jin.practice.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<List<HospitalDto>> list() {
        List<HospitalDto> response = hospitalService.findAll();

        return ResponseEntity.ok(response);
    }
}
