package com.jin.practice.reception.entity;

import com.jin.practice.hospital.entity.Hospital;
import com.jin.practice.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reception",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reception_hospital_date_queue_number",
                columnNames = {"hospital_id", "queue_date", "queue_number"}
        )
)
@NoArgsConstructor
@Getter
public class Reception {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 50)
    private VisitType visitType;

    @Column(name = "symptom", nullable = false, length = 500)
    private String symptom;

    @Column(name = "queue_number", nullable = false)
    private int queueNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_status", nullable = false, length = 50)
    private QueueStatus queueStatus;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Column(name = "queue_time", nullable = false)
    private LocalDateTime queueTime;

    @Column(name = "called_time")
    private LocalDateTime calledTime;

    @Column(name = "done_time")
    private LocalDateTime doneTime;

    @Column(name = "canceled_time")
    private LocalDateTime canceledTime;

    public Reception(
            Member member,
            Hospital hospital,
            String patientName,
            VisitType visitType,
            String symptom,
            int queueNumber,
            LocalDate queueDate,
            LocalDateTime queueTime
    ) {
        this.member = member;
        this.hospital = hospital;
        this.patientName = patientName;
        this.visitType = visitType;
        this.symptom = symptom;
        this.queueNumber = queueNumber;
        this.queueDate = queueDate;
        this.queueTime = queueTime;
        this.queueStatus = QueueStatus.WAITING;
    }

    public void call() {
        this.queueStatus = QueueStatus.CALLED;
        this.calledTime = LocalDateTime.now();
    }

    public void complete() {
        this.queueStatus = QueueStatus.COMPLETED;
        this.doneTime = LocalDateTime.now();
    }

    public void cancel() {
        this.queueStatus = QueueStatus.CANCELED;
        this.canceledTime = LocalDateTime.now();
    }
}
