package com.jin.practice.hospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "hospital")
@NoArgsConstructor
@Getter
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long id;

    @Column(name = "hospital_name", nullable = false)
    private String name;

    @Column(name = "hospital_addr", nullable = false)
    private String addr;

    @Column(name = "hospital_number", nullable = false, unique = true)
    private String number;

    @Column(name = "open_status")
    private boolean openStatus = true;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "lunch_start_time")
    private LocalTime lunchStartTime;

    @Column(name = "lunch_end_time")
    private LocalTime lunchEndTime;

    @Column(name = "closed_days", length = 100)
    private String closedDays;

    @Column(name = "waiting_people")
    private int waitingPeople;

    @Column(name = "waiting_time")
    private int waitingTime;

    @Column(name = "rating_sum")
    private int ratingSum;

    @Column(name = "rating_count")
    private int ratingCount;

    public double getRating() {
        if(ratingCount == 0)
            return 0.0;

        return Math.round(((double) ratingSum/ratingCount)*10) / 10.0;
    }

    public void updateWaitingStats(int waitingPeople, int waitingTime) {
        this.waitingPeople = waitingPeople;
        this.waitingTime = waitingTime;
    }

    public boolean acceptsReception(LocalDate date, LocalTime time) {
        if (!openStatus || isClosedDay(date.getDayOfWeek())) {
            return false;
        }

        if (openTime != null && time.isBefore(openTime)) {
            return false;
        }

        if (closeTime != null && !time.isBefore(closeTime)) {
            return false;
        }

        return !isLunchTime(time);
    }

    private boolean isClosedDay(DayOfWeek dayOfWeek) {
        return parseClosedDays().contains(dayOfWeek);
    }

    private boolean isLunchTime(LocalTime time) {
        if (lunchStartTime == null || lunchEndTime == null) {
            return false;
        }

        return !time.isBefore(lunchStartTime) && time.isBefore(lunchEndTime);
    }

    private Set<DayOfWeek> parseClosedDays() {
        if (closedDays == null || closedDays.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(closedDays.split(","))
                .map(String::trim)
                .filter(day -> !day.isBlank())
                .map(String::toUpperCase)
                .map(this::parseDayOfWeek)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private DayOfWeek parseDayOfWeek(String day) {
        try {
            return DayOfWeek.valueOf(day);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
