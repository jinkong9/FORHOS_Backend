package com.jin.practice.reception.service;

import java.time.LocalDate;

public interface QueueNumberGenerator {
    int next(Long hospitalId, LocalDate date);
}
