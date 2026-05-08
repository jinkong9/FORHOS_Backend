package com.jin.practice.hospital.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.dto.HospitalDto;
import com.jin.practice.hospital.entity.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public List<HospitalDto> findAll() {
        return hospitalRepository.findAll()// DB에서 Entity List 가져옴
                .stream() // 리스트에서 Stream 사용
                .map(HospitalDto::from) // Entity들 Dto로 변환
                .toList(); // List로 변환
    }

    public HospitalDto getHospitalByName(String name){ // 병원 이름 검색
        Hospital hospital = hospitalRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 병원을 찾을 수 없습니다."));

        return HospitalDto.from(hospital);
    }

}
