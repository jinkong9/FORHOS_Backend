package com.jin.practice.hospital.entity;

import com.jin.practice.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hospital")
@NoArgsConstructor
@Getter
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long id;

    @Column(name = "hospital_name", nullable = false)
    private String name;

    @Column(name = "hospital_addr", nullable = false)
    private String addr;

    @Column(name = "hospital_number", nullable = false, unique = true)
    private String number;

    @Column(name = "open_status")
    private boolean openStatus = true;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

}
