package com.jin.practice.hospital.dto;

import org.springframework.data.domain.Sort;

public enum HospitalSortOption {
    ID_ASC(Sort.by(Sort.Direction.ASC, "id")),
    NAME_ASC(Sort.by(Sort.Direction.ASC, "name")),
    WAITING_PEOPLE_ASC(Sort.by(Sort.Direction.ASC, "waitingPeople")),
    WAITING_TIME_ASC(Sort.by(Sort.Direction.ASC, "waitingTime")),
    RATING_DESC(Sort.by(Sort.Direction.DESC, "ratingSum")
            .and(Sort.by(Sort.Direction.DESC, "ratingCount")));

    private final Sort sort;

    HospitalSortOption(Sort sort) {
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }
}
