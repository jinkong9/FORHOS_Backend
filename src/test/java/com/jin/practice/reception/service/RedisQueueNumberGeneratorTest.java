package com.jin.practice.reception.service;

import com.jin.practice.reception.Repository.ReceptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisQueueNumberGeneratorTest {

    @Test
    void nextInitializesMissingRedisKeyFromDatabaseAndIncrementsDailyHospitalQueueNumber() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        RedisQueueNumberGenerator generator = new RedisQueueNumberGenerator(redisTemplate, receptionRepository);

        when(redisTemplate.hasKey("reception:queue-number:3:20260712")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(receptionRepository.findMaxQueueNumberByHospitalIdAndQueueDate(
                3L,
                LocalDate.of(2026, 7, 12)
        )).thenReturn(6);
        when(valueOperations.increment("reception:queue-number:3:20260712")).thenReturn(7L);

        int queueNumber = generator.next(3L, LocalDate.of(2026, 7, 12));

        assertThat(queueNumber).isEqualTo(7);
        verify(valueOperations).setIfAbsent(
                "reception:queue-number:3:20260712",
                "6",
                Duration.ofDays(2)
        );
        verify(valueOperations).increment("reception:queue-number:3:20260712");
        verify(redisTemplate).expire("reception:queue-number:3:20260712", Duration.ofDays(2));
    }

    @Test
    void nextFallsBackToDatabaseWhenRedisIncrementFails() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        ReceptionRepository receptionRepository = mock(ReceptionRepository.class);
        RedisQueueNumberGenerator generator = new RedisQueueNumberGenerator(redisTemplate, receptionRepository);

        when(redisTemplate.hasKey("reception:queue-number:3:20260712")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("reception:queue-number:3:20260712"))
                .thenThrow(new IllegalStateException("Redis unavailable"));
        when(receptionRepository.findMaxQueueNumberByHospitalIdAndQueueDate(
                3L,
                LocalDate.of(2026, 7, 12)
        )).thenReturn(9);

        int queueNumber = generator.next(3L, LocalDate.of(2026, 7, 12));

        assertThat(queueNumber).isEqualTo(10);
    }
}
