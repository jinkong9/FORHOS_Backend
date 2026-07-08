package com.jin.practice.hospital.service;

import com.jin.practice.hospital.Repository.HospitalRepository;
import com.jin.practice.hospital.dto.HospitalDto;
import com.jin.practice.hospital.dto.HospitalSortOption;
import com.jin.practice.hospital.entity.Hospital;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HospitalServiceTest {

    @Test
    void searchTrimsKeywordAndAppliesSortOption() {
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        HospitalService hospitalService = new HospitalService(hospitalRepository);
        Pageable pageable = PageRequest.of(0, 10);
        Hospital hospital = hospital();
        PageRequest expectedPageable = PageRequest.of(
                0,
                10,
                HospitalSortOption.WAITING_TIME_ASC.toSort()
        );

        when(hospitalRepository.search(eq("서울"), eq(true), eq(expectedPageable)))
                .thenReturn(new PageImpl<>(List.of(hospital), expectedPageable, 1));

        Page<HospitalDto> response = hospitalService.search(
                " 서울 ",
                true,
                HospitalSortOption.WAITING_TIME_ASC,
                pageable
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().name()).isEqualTo("FORHOS Hospital");
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchDefaultsBlankKeywordAndNullSort() {
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        HospitalService hospitalService = new HospitalService(hospitalRepository);
        Pageable pageable = PageRequest.of(1, 5);
        Hospital hospital = hospital();
        PageRequest expectedPageable = PageRequest.of(
                1,
                5,
                HospitalSortOption.ID_ASC.toSort()
        );

        when(hospitalRepository.search(eq(null), eq(false), eq(expectedPageable)))
                .thenReturn(new PageImpl<>(List.of(hospital), expectedPageable, 1));

        Page<HospitalDto> response = hospitalService.search(
                " ",
                false,
                null,
                pageable
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getNumber()).isEqualTo(1);
    }

    private Hospital hospital() {
        Hospital hospital = new Hospital();
        ReflectionTestUtils.setField(hospital, "id", 1L);
        ReflectionTestUtils.setField(hospital, "name", "FORHOS Hospital");
        ReflectionTestUtils.setField(hospital, "addr", "Seoul");
        ReflectionTestUtils.setField(hospital, "number", "02-1234-5678");
        ReflectionTestUtils.setField(hospital, "openStatus", true);
        ReflectionTestUtils.setField(hospital, "waitingPeople", 3);
        ReflectionTestUtils.setField(hospital, "waitingTime", 15);
        return hospital;
    }
}
