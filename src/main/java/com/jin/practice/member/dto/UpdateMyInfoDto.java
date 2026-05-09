package com.jin.practice.member.dto;

public record UpdateMyInfoDto(
        String name,
        int age,
        String gender,
        String phone,
        String region,
        String extra
) {

}
