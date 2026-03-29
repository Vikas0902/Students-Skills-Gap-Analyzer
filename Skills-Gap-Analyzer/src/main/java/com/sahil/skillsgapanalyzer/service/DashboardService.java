package com.sahil.skillsgapanalyzer.service;

import com.sahil.skillsgapanalyzer.integration.MlPredictionClient;
import com.sahil.skillsgapanalyzer.repository.StudentAttemptRepository;
import com.sahil.skillsgapanalyzer.repository.StudentRepository;
import org.springframework.stereotype.Service;

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
}
