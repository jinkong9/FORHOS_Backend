package com.jin.practice.reception.Repository;

import com.jin.practice.member.entity.Member;
import com.jin.practice.reception.entity.QueueStatus;
import com.jin.practice.reception.entity.Reception;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {
    List<Reception> findByHospital_IdAndQueueDate(
            Long hospitalId,
            LocalDate queueDate
    );

    List<Reception> findByQueueDate(LocalDate queueDate);

    Page<Reception> findByQueueDate(
            LocalDate queueDate,
            Pageable pageable
    );

    Page<Reception> findByQueueDateAndQueueStatus(
            LocalDate queueDate,
            QueueStatus queueStatus,
            Pageable pageable
    );

    Page<Reception> findByHospital_IdAndQueueDate(
            Long hospitalId,
            LocalDate queueDate,
            Pageable pageable
    );

    Page<Reception> findByHospital_IdAndQueueDateAndQueueStatus(
            Long hospitalId,
            LocalDate queueDate,
            QueueStatus queueStatus,
            Pageable pageable
    );

    List<Reception> findTop10ByHospital_IdAndQueueStatusAndCalledTimeIsNotNullAndDoneTimeIsNotNullOrderByDoneTimeDesc(
            Long hospitalId,
            QueueStatus queueStatus
    );

    boolean existsByMemberAndHospital_IdAndQueueDateAndQueueStatusIn(
            Member member,
            Long hospitalId,
            LocalDate queueDate,
            List<QueueStatus> queueStatuses
    );

    List<Reception> findByMember(Member member);
}
