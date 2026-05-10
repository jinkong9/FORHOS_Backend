package com.jin.practice.reception.Repository;

import com.jin.practice.reception.entity.Reception;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceptionRepository extends JpaRepository<Reception, Long> {
//    findLastQueue()
//    findCurrentQueue()
//    findMine()
//    findTodayQueues()

}
