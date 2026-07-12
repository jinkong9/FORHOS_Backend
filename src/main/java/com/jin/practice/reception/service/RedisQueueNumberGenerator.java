package com.jin.practice.reception.service;

import com.jin.practice.reception.Repository.ReceptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class RedisQueueNumberGenerator implements QueueNumberGenerator {
    private static final String KEY_PREFIX = "reception:queue-number";
    private static final Duration KEY_TTL = Duration.ofDays(2);

    private final StringRedisTemplate redisTemplate;
    private final ReceptionRepository receptionRepository;

    public RedisQueueNumberGenerator(
            StringRedisTemplate redisTemplate,
            ReceptionRepository receptionRepository
    ) {
        this.redisTemplate = redisTemplate;
        this.receptionRepository = receptionRepository;
    }

    @Override
    public int next(Long hospitalId, LocalDate date) {
        String key = buildKey(hospitalId, date);

        try {
            initializeIfAbsent(key, hospitalId, date);
            Long value = redisTemplate.opsForValue().increment(key);

            if (value == null) {
                throw new IllegalStateException("대기번호 발급에 실패했습니다.");
            }

            redisTemplate.expire(key, KEY_TTL);
            return Math.toIntExact(value);
        } catch (RuntimeException exception) {
            log.warn("Redis 대기번호 발급 실패. DB 기반 발급으로 fallback합니다. key={}", key, exception);
            return nextFromDatabase(hospitalId, date);
        }
    }

    private void initializeIfAbsent(String key, Long hospitalId, LocalDate date) {
        Boolean hasKey = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(hasKey)) {
            return;
        }

        int maxQueueNumber = currentMaxQueueNumber(hospitalId, date);
        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(maxQueueNumber), KEY_TTL);
    }

    private int nextFromDatabase(Long hospitalId, LocalDate date) {
        return currentMaxQueueNumber(hospitalId, date) + 1;
    }

    private int currentMaxQueueNumber(Long hospitalId, LocalDate date) {
        return receptionRepository.findMaxQueueNumberByHospitalIdAndQueueDate(hospitalId, date);
    }

    private String buildKey(Long hospitalId, LocalDate date) {
        return "%s:%d:%s".formatted(
                KEY_PREFIX,
                hospitalId,
                date.format(DateTimeFormatter.BASIC_ISO_DATE)
        );
    }
}
