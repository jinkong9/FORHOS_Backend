package com.jin.practice.hospital.Repository;

import com.jin.practice.hospital.entity.Hospital;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital,Long> {
    Optional<Hospital> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hospital h where h.id = :id")
    Optional<Hospital> findByIdForUpdate(@Param("id") Long id);
}
