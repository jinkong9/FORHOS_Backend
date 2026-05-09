package com.jin.practice.member.dto;

import com.jin.practice.member.entity.Member;

public record MyInfoDto(
        String name,
        int age,
        String gender,
        String phone,
        String region,
        String extra
) {
    public static MyInfoDto from(Member member) {
        return new MyInfoDto(
                member.getName(),
                member.getAge(),
                member.getGender(),
                member.getPhone(),
                member.getRegion(),
                member.getExtra()
        );
    }

}
