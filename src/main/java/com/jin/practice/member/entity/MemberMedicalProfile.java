package com.jin.practice.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class MemberMedicalProfile {

    @Column(name = "medicines", length = 500)
    private String medicines = "";

    @Column(name = "diseases", length = 500)
    private String diseases = "";

    @Column(name = "allergies", length = 500)
    private String allergies = "";

    @Column(name = "medical_notes", length = 500)
    private String notes = "";

    public MemberMedicalProfile(String medicines, String diseases, String allergies, String notes) {
        this.medicines = normalize(medicines);
        this.diseases = normalize(diseases);
        this.allergies = normalize(allergies);
        this.notes = normalize(notes);
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
