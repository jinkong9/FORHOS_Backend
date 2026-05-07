package com.jin.practice.member.Repository;

import com.jin.practice.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findbyEmail(String email);
}
