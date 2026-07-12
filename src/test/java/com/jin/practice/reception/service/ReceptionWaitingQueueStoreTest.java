package com.jin.practice.reception.service;

import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.entity.Member;
import com.jin.practice.member.entity.MemberRole;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import com.jin.practice.reception.entity.VisitType;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceptionWaitingQueueStoreTest {

    @Test
    void addWaitingStoresReceptionIdWithQueueNumberScore() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
        ReceptionWaitingQueueStore store = new ReceptionWaitingQueueStore(redisTemplate);
        Reception reception = waitingReception(7L, 3);

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        store.addWaiting(reception);

        verify(zSetOperations).add("reception:waiting:1:20260712", "7", 3);
        verify(redisTemplate).expire("reception:waiting:1:20260712", Duration.ofDays(2));
    }

    @Test
    void addWaitingIgnoresNonWaitingReception() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ReceptionWaitingQueueStore store = new ReceptionWaitingQueueStore(redisTemplate);
        Reception reception = waitingReception(7L, 3);
        reception.call();

        store.addWaiting(reception);

        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    void countWaitingBeforeUsesSortedSetRank() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
        ReceptionWaitingQueueStore store = new ReceptionWaitingQueueStore(redisTemplate);
        Reception reception = waitingReception(7L, 3);

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rank("reception:waiting:1:20260712", "7")).thenReturn(2L);

        Optional<Integer> waitingCount = store.countWaitingBefore(reception);

        assertThat(waitingCount).contains(2);
    }

    @Test
    void findWaitingReceptionIdsReturnsSortedIdsWhenRedisKeyExists() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
        ReceptionWaitingQueueStore store = new ReceptionWaitingQueueStore(redisTemplate);
        Set<String> ids = new LinkedHashSet<>(List.of("10", "11"));

        when(redisTemplate.hasKey("reception:waiting:1:20260712")).thenReturn(true);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.range("reception:waiting:1:20260712", 0, -1)).thenReturn(ids);

        Optional<List<Long>> response = store.findWaitingReceptionIds(1L, LocalDate.of(2026, 7, 12));

        assertThat(response).contains(List.of(10L, 11L));
    }

    private Reception waitingReception(Long id, int queueNumber) {
        Member member = new Member(
                "user@example.com",
                "encoded-password",
                "User",
                30,
                "010-1234-5678",
                "MALE",
                "Seoul",
                "none",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "role", MemberRole.USER);

        Hospital hospital = new Hospital();
        ReflectionTestUtils.setField(hospital, "id", 1L);
        ReflectionTestUtils.setField(hospital, "name", "FORHOS Hospital");

        Reception reception = new Reception(
                member,
                hospital,
                "Patient",
                VisitType.FIRST,
                "Headache",
                queueNumber,
                LocalDate.of(2026, 7, 12),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(reception, "id", id);
        ReflectionTestUtils.setField(reception, "queueStatus", QueueStatus.WAITING);
        return reception;
    }
}
