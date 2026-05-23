package com.jin.practice.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@NoArgsConstructor
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column
    private LocalDateTime create_at;

    @Column(nullable = false, length = 50)
    private String gender;

    @Column(length = 50)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberRole role = MemberRole.USER;

    @Column
    private String extra;

    public Member(String email, String password, String name, int age, String phone, String gender, String region, String extra, LocalDateTime create_at) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.gender = gender;
        this.region = region;
        this.extra = extra;
        this.create_at = create_at;
    }

    public void updateInfo(String name, Integer age, String gender, String phone, String region, String extra) {
        if (name != null) this.name = name;
        if (age != null) this.age = age;
        if (gender != null) this.gender = gender;
        if (phone != null) this.phone = phone;
        if (region != null) this.region = region;
        if (extra != null) this.extra = extra;
    }
}
