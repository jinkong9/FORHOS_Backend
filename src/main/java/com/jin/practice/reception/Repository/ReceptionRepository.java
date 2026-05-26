package com.jin.practice.reception.Repository;

import com.jin.practice.member.entity.Member;
import com.jin.practice.reception.entity.Reception;
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

    List<Reception> findByMember(Member member);
}
