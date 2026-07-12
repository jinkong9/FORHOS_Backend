package com.jin.practice.reception.service;

import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceptionWaitingQueueStore {
    private static final String KEY_PREFIX = "reception:waiting";
    private static final Duration KEY_TTL = Duration.ofDays(2);

    private final StringRedisTemplate redisTemplate;

    public void addWaiting(Reception reception) {
        if (reception.getQueueStatus() != QueueStatus.WAITING || reception.getId() == null) {
            return;
        }

        String key = buildKey(reception.getHospital().getId(), reception.getQueueDate());
        String value = encodeReceptionId(reception.getId());

        try {
            redisTemplate.opsForZSet().add(key, value, reception.getQueueNumber());
            redisTemplate.expire(key, KEY_TTL);
        } catch (RuntimeException exception) {
            log.warn("Redis 대기열 추가 실패. key={}, receptionId={}", key, reception.getId(), exception);
        }
    }

    public void remove(Reception reception) {
        if (reception.getId() == null) {
            return;
        }

        String key = buildKey(reception.getHospital().getId(), reception.getQueueDate());
        String value = encodeReceptionId(reception.getId());

        try {
            redisTemplate.opsForZSet().remove(key, value);
            redisTemplate.expire(key, KEY_TTL);
        } catch (RuntimeException exception) {
            log.warn("Redis 대기열 제거 실패. key={}, receptionId={}", key, reception.getId(), exception);
        }
    }

    public Optional<Integer> countWaitingBefore(Reception reception) {
        if (reception.getQueueStatus() != QueueStatus.WAITING || reception.getId() == null) {
            return Optional.of(0);
        }

        String key = buildKey(reception.getHospital().getId(), reception.getQueueDate());
        String value = encodeReceptionId(reception.getId());

        try {
            Long rank = redisTemplate.opsForZSet().rank(key, value);

            if (rank == null) {
                return Optional.empty();
            }

            return Optional.of(Math.toIntExact(rank));
        } catch (RuntimeException exception) {
            log.warn("Redis 대기 순위 조회 실패. key={}, receptionId={}", key, reception.getId(), exception);
            return Optional.empty();
        }
    }

    public Optional<List<Long>> findWaitingReceptionIds(Long hospitalId, LocalDate date) {
        String key = buildKey(hospitalId, date);

        try {
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return Optional.empty();
            }

            Set<String> values = redisTemplate.opsForZSet().range(key, 0, -1);

            if (values == null) {
                return Optional.empty();
            }

            List<Long> receptionIds = new ArrayList<>();
            for (String value : values) {
                parseReceptionId(value).ifPresent(receptionIds::add);
            }

            return Optional.of(receptionIds);
        } catch (RuntimeException exception) {
            log.warn("Redis 대기열 조회 실패. key={}", key, exception);
            return Optional.empty();
        }
    }

    public void replaceWaitingQueue(Long hospitalId, LocalDate date, List<Reception> receptions) {
        String key = buildKey(hospitalId, date);

        try {
            redisTemplate.delete(key);

            for (Reception reception : receptions) {
                if (reception.getQueueStatus() == QueueStatus.WAITING && reception.getId() != null) {
                    redisTemplate.opsForZSet().add(
                            key,
                            encodeReceptionId(reception.getId()),
                            reception.getQueueNumber()
                    );
                }
            }

            redisTemplate.expire(key, KEY_TTL);
        } catch (RuntimeException exception) {
            log.warn("Redis 대기열 재구성 실패. key={}", key, exception);
        }
    }

    private String buildKey(Long hospitalId, LocalDate date) {
        return "%s:%d:%s".formatted(
                KEY_PREFIX,
                hospitalId,
                date.format(DateTimeFormatter.BASIC_ISO_DATE)
        );
    }

    private String encodeReceptionId(Long receptionId) {
        return String.valueOf(receptionId);
    }

    private Optional<Long> parseReceptionId(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            log.warn("Redis 대기열에 잘못된 receptionId 값이 있습니다. value={}", value);
            return Optional.empty();
        }
    }
}
