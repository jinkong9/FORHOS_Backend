package com.jin.practice.hospital.Repository;

import com.jin.practice.hospital.entity.Hospital;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital,Long> {
    Optional<Hospital> findByName(String name);

    @Query("""
            select h from Hospital h
            where (:keyword is null
                or lower(h.name) like lower(concat('%', :keyword, '%'))
                or lower(h.addr) like lower(concat('%', :keyword, '%'))
                or h.number like concat('%', :keyword, '%'))
            and (:openOnly = false or h.openStatus = true)
            """)
    Page<Hospital> search(
            @Param("keyword") String keyword,
            @Param("openOnly") boolean openOnly,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hospital h where h.id = :id")
    Optional<Hospital> findByIdForUpdate(@Param("id") Long id);
}
