package com.jin.practice.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@NoArgsConstructor
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String phone;

    @Column
    private LocalDate create_at;

    public Member(String email, String password, String name, String phone, LocalDate create_at) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.create_at = create_at;
    }
}
