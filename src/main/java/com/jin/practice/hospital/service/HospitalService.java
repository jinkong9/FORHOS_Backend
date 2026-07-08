package com.jin.practice.hospital.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.dto.HospitalDto;
import com.jin.practice.hospital.dto.HospitalSortOption;
import com.jin.practice.hospital.entity.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public List<HospitalDto> findAll() {
        return hospitalRepository.findAll()
                .stream()
                .map(HospitalDto::from)
                .toList();
    }

    public Page<HospitalDto> search(
            String keyword,
            boolean openOnly,
            HospitalSortOption sort,
            Pageable pageable
    ) {
        HospitalSortOption sortOption = sort == null ? HospitalSortOption.ID_ASC : sort;
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortOption.toSort()
        );

        return hospitalRepository.search(normalizeKeyword(keyword), openOnly, sortedPageable)
                .map(HospitalDto::from);
    }

    public HospitalDto getHospitalByName(String name) {
        Hospital hospital = hospitalRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 병원을 찾을 수 없습니다."));

        return HospitalDto.from(hospital);
    }

    public HospitalDto findById(long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "병원을 찾을 수 없습니다."));

        return HospitalDto.from(hospital);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }
}
