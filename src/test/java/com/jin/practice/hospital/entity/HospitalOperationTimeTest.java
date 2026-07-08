package com.jin.practice.hospital.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class HospitalOperationTimeTest {

    @Test
    void acceptsReceptionInsideOperationTime() {
        Hospital hospital = hospitalWithOperationTime();

        boolean acceptsReception = hospital.acceptsReception(
                LocalDate.of(2026, 7, 8),
                LocalTime.of(10, 0)
        );

        assertThat(acceptsReception).isTrue();
    }

    @Test
    void rejectsReceptionDuringLunchTime() {
        Hospital hospital = hospitalWithOperationTime();

        boolean acceptsReception = hospital.acceptsReception(
                LocalDate.of(2026, 7, 8),
                LocalTime.of(12, 45)
        );

        assertThat(acceptsReception).isFalse();
    }

    @Test
    void rejectsReceptionOnClosedDay() {
        Hospital hospital = hospitalWithOperationTime();
        ReflectionTestUtils.setField(hospital, "closedDays", "SUNDAY,WEDNESDAY");

        boolean acceptsReception = hospital.acceptsReception(
                LocalDate.of(2026, 7, 8),
                LocalTime.of(10, 0)
        );

        assertThat(acceptsReception).isFalse();
    }

    private Hospital hospitalWithOperationTime() {
        Hospital hospital = new Hospital();
        ReflectionTestUtils.setField(hospital, "openStatus", true);
        ReflectionTestUtils.setField(hospital, "openTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(hospital, "closeTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(hospital, "lunchStartTime", LocalTime.of(12, 30));
        ReflectionTestUtils.setField(hospital, "lunchEndTime", LocalTime.of(13, 30));
        ReflectionTestUtils.setField(hospital, "closedDays", "SUNDAY");
        return hospital;
    }
}
