package com.jin.practice.hospital.entity;

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

    @Column(name = "waiting_people")
    private int waitingPeople;

    @Column(name = "waiting_time")
    private int waitingTime;

    @Column(name = "rating_sum")
    private int ratingSum;

    @Column(name = "rating_count")
    private int ratingCount;

    public double getRating() {
        if(ratingCount == 0)
            return 0.0;

        return Math.round(((double) ratingSum/ratingCount)*10) / 10.0;
    }

    public void updateWaitingStats(int waitingPeople, int waitingTime) {
        this.waitingPeople = waitingPeople;
        this.waitingTime = waitingTime;
    }
}
