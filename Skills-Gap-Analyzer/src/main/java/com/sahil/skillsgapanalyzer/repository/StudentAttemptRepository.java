package com.sahil.skillsgapanalyzer.repository;

import com.sahil.skillsgapanalyzer.entity.StudentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAttemptRepository extends JpaRepository<StudentAttempt , Long>{
    List<StudentAttempt> findByStudent_IdOrderByActionNumAsc(Long studentId);
}
