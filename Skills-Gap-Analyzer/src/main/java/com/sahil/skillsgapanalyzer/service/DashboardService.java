package com.sahil.skillsgapanalyzer.service;

import com.sahil.skillsgapanalyzer.dto.DashboardDataResponse;
import com.sahil.skillsgapanalyzer.entity.Student;
import com.sahil.skillsgapanalyzer.entity.StudentAttempt;
import com.sahil.skillsgapanalyzer.integration.MlPredictionClient;
import com.sahil.skillsgapanalyzer.repository.StudentAttemptRepository;
import com.sahil.skillsgapanalyzer.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final StudentAttemptRepository studentAttemptRepository;
    private final MlPredictionClient mlPredictionClient;

    //    Constructor Injection
    public DashboardService(StudentRepository studentRepository,
                            StudentAttemptRepository studentAttemptRepository,
                            MlPredictionClient mlPredictionClient){
        this.studentRepository = studentRepository;
        this.studentAttemptRepository = studentAttemptRepository;
        this.mlPredictionClient = mlPredictionClient;
    }

    public DashboardDataResponse getStudentDashboard(Long studentId){
//        Fetching student and their attempt history:
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student with id: "+studentId+" not found!"));

        List<StudentAttempt> history = studentAttemptRepository.findByStudent_IdOrderByActionNumAsc(studentId);

        if(history.isEmpty()){
            throw new RuntimeException("No attempt history found for this student");
        }

//    --Calculate Overall Metrics
        int totalAttempts = history.size();
        long totalCorrect = history.stream().filter(a -> a.getCorrect() == 1).count();
        double overallAccuracy = (double) totalCorrect/totalAttempts;

//    --Calculate recent accuracy (last 3 attempts)
        int recentCount  = Math.min(3 , totalAttempts);
        List<StudentAttempt> recentAttempts = history.subList(totalAttempts - recentCount , totalAttempts);
        long recentCorrect = recentAttempts.stream().filter(a -> a.getCorrect() == 1).count();
        double recentAccuracy = (double) recentCorrect/recentCount;

//     --Group by skill to calculate specific mastery

    }
}
