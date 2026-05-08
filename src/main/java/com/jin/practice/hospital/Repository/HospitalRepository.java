package com.jin.practice.hospital.Repository;

import com.jin.practice.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital,Long> {
    Optional<Hospital> findByName(String name);
}
