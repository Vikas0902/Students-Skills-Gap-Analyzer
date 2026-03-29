package com.sahil.skillsgapanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @Column(name = "student_id")
    private double id;

    @OneToMany(mappedBy = "student" , cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private List<StudentAttempt> attempts;

}
