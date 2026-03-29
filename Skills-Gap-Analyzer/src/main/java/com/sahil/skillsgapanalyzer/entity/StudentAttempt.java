package com.sahil.skillsgapanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.Inet4Address;

@Entity
@Table(name = "student_attempts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id" , nullable = false)
    private Student student;

    @Column(nullable = false)
    private String skill;

    @Column(nullable = false)
    private Integer correct;

    @Column(name = "action_num")
    private Long actionNum;

    @Column(name = "hint_count")
    private Integer hintCount;

    @Column(name = "time_taken")
    private Double timeTaken;
}
